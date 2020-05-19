//@ts-check
const axios = require('axios').default;
const path = require('path');
const fs = require('fs');

function toLowerCamelCase(str) {
  str = toUpperCamelCase(str);
  return str[0].toLowerCase() + str.substr(1);
}
function toUpperCamelCase(str) {
  return str.replace(/[-_](\w)/g, (a, b) => b.toUpperCase());
}
function normalSwagger(str) {
  str = str.replace(/«/g, 'Of').replace(/»/g, '');
  // str = str.replace(/[^List|<List]<([^>]*)>/g, (a, b) => b + '[]');
  return str;
}

class ApiCodeGenerator {
  constructor(config) {
    this.config = config;
    this.apiDocs = {};
    this.apiClasses = {};
    this.typeDefinitionMap = new Map();
  }
  parsePath(path, pathInfo) {
    let pathParts = path.split('/').slice(this.config.pathSplitIndex);
    for (let httpMethod in pathInfo) {
      let { parameters = [], responses, produces } = pathInfo[httpMethod];
      if (Array.isArray(produces) && produces[0] === 'application/octet-stream') continue;
      try {
        let apiMethod = this.apiClasses;
        for (let i = 0; i < pathParts.length; i++) {
          let pathPart = pathParts[i];
          let name = toLowerCamelCase(pathPart);

          //todo 支持restful
          if (pathPart[0] == '{' && pathParts.length - 1 == i) {
            name = toLowerCamelCase(httpMethod);
          } else if (pathPart[0] == '{') {
            continue;
          }
          if (!apiMethod[name]) {
            apiMethod[name] = {};
          }
          apiMethod = apiMethod[name];
        }
        if (!apiMethod) throw `无法生成api class`;
        let responseType = this.getType(responses[200].schema);
        let params = {};
        for (let p of parameters) {
          let { in: paramIn, name } = p;
          if (paramIn == 'header' || paramIn == 'formData') {
            continue;
          }
          let type = this.getType(p);
          params[name] = { ...p, type };
        }
        Object.assign(apiMethod, {
          ...pathInfo[httpMethod],
          httpMethod,
          responseType,
          path,
          params,
        });
      } catch (ex) {
        throw `${path}${JSON.stringify(responses[200])}${ex}`;
      }
    }
  }

  async codegen() {
    let { name, url, codegenType } = this.config;
    console.log('start codegen ', name, url);
    let apiurl = url.replace('/swagger-ui.html', '/v2/api-docs');
    try {
      let res = await axios.get(apiurl);
      this.apiDocs = res.data;
      let { paths } = this.apiDocs;
      for (let path in paths) {
        this.parsePath(path, paths[path]);
      }
    } catch (ex) {
      console.log(this.config.url, ex);
    }
    // console.warn('api class', JSON.stringify(this.apiClasses));
    let codeGen = require(`./apigen.${codegenType}.js`);
    let fileContent = codeGen(this.apiClasses, this.typeDefinitionMap, this.config);
    const apiDir = path.join(process.cwd(), 'src', 'api');
    fs.writeFileSync(path.join(apiDir, this.config.name + `.${codegenType}`), fileContent, 'utf8');
  }

  getType(property) {
    let { type, $ref, format, schema } = property;
    if (schema) return this.getType(schema);
    if ($ref) return this.getRefType($ref);
    switch (type) {
      case 'integer':
      case 'number':
      case 'long':
      case 'int64':
      case 'int':
        if (format == 'int64') return 'Int64';
        return 'number';
      case 'string':
        return 'string';
      case 'boolean':
        return 'boolean';
      case 'list':
      case 'array':
        let refType = this.getType(property.items);
        return `List<${refType}>`;
      case 'object':
        if (property.additionalProperties) return 'any';
        throw new Error(`无法识别的object类型${JSON.stringify(property)}`);
      default:
        throw new Error(`无法识别的属性类型 ${type} ${JSON.stringify(property)}`);
    }
  }

  addTypeDefinition(name, definition) {
    let properties = {};
    let required = definition.required || [];

    for (let propertyName in definition.properties) {
      let property = definition.properties[propertyName];
      try {
        let tsType = this.getType(property);
        let { description = '', example = '' } = property;
        description = `${description}${
          example ? ' example:' + example.toString().replace(/\*/g, '') : ''
        }`;
        properties[propertyName] = {
          propertyName,
          required: required.includes(propertyName),
          type: tsType,
          description,
        };
      } catch (ex) {
        console.error(ex);
        throw `无法识别的ref类型${propertyName} ${JSON.stringify(definition)}`;
      }
    }
    let typeDefinition = {
      description: definition.description,
      properties,
      name,
    };
    this.typeDefinitionMap.set(name, typeDefinition);
  }

  getRefType(refType) {
    let defineType = refType.replace('#/definitions/', '');
    let definition = this.apiDocs.definitions[defineType];
    let tsType = normalSwagger(defineType);

    if (definition) {
      this.addTypeDefinition(tsType, definition);
    } else {
      throw 'invalidate types:' + refType;
    }

    return tsType;
  }
}

let defaultConfig = {
  mapTypes: { Timestamp: 'number', Int64: 'string' },
  codegenType: 'ts',
  pathSplitIndex: 2,
  ignoreTypes: [],
};
let config = require(path.join(process.cwd(), 'scripts', 'api', 'api.config.js'));
let { apis = [], ...restConfig } = { ...defaultConfig, ...config };
apis.forEach(api => {
  let apiCodegen = new ApiCodeGenerator({ ...api, ...restConfig });
  apiCodegen.codegen();
});
