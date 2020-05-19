// @ts-check
const path = require('path');
const fs = require('fs');
const prettier = require('prettier');

function codegen(apiClasses, typeDefinitionMap, config) {
  let tsContent = [];
  tsContent.push('type Int64=string');
  tsContent.push('type long=number');
  tsContent.push('type int=number');

  tsContent.push('type List<T>=Array<T>');
  let typeNames = [...typeDefinitionMap.keys()].sort();
  // 生成ts类型
  for (let typeName of typeNames) {
    if (typeName.startsWith(config.responseWarp)) continue;
    let typeInfo = typeDefinitionMap.get(typeName);
    //排除Map定义
    if (typeInfo.typeName === 'Map<T>') continue;
    if (config.ignoreTypes.some(i => i == typeName)) continue;
    if (typeInfo.description) {
      tsContent.push(`/** ${typeInfo.description} */`);
    }
    tsContent.push(`export interface ${typeName} {`);
    for (let propertyName in typeInfo.properties) {
      let property = typeInfo.properties[propertyName];
      if (property.description) {
        tsContent.push(`/** ${property.description} */`);
      }
      tsContent.push(`${propertyName}${property.required ? '' : '?'}: ${property.type}`);
    }

    tsContent.push('}\n');
  }
  //生成api
  let apiContent = [`import http from 'services/http'`];
  apiContent.push(`let baseUrl='${config.url.match(/(http:\/\/[^\/]*)/)[0]}'`);
  apiContent.push('export default {');
  let classNames = Object.keys(apiClasses);

  if (classNames.length == 1 && classNames[0].toLowerCase() == config.name.toLowerCase())
    generateApi(apiContent, apiClasses[classNames[0]], config);
  else {
    generateApi(apiContent, apiClasses, config);
  }
  apiContent.push('}');

  let fileContent = tsContent.join('\n') + apiContent.join('\n');
  // let options = prettier.resolveConfig.sync(path.join(process.cwd(), 'prettier.config.js'));
  // fileContent = prettier.format(fileContent, { ...options, parser: 'typescript' });
  return fileContent;
}
function generateApi(arr, allclasss, config) {
  for (let className in allclasss) {
    let apiClass = allclasss[className];
    if (apiClass.params && apiClass.responseType) {
      generateClass(arr, apiClass, className, config);
      continue;
    }
    arr.push(`${className}: {`);
    generateApi(arr, apiClass, config);
    arr.push(`},`);
  }
}
function generateClass(arr, apiClass, methodName, config) {
  let { path, httpMethod, responseType, summary, body, params } = apiClass;
  let queryParamArr = [];
  let bodyParamArr = [];
  for (let paramName in params) {
    let param = params[paramName];
    if (param.in === 'query' || param.in === 'path') {
      if (param.description) {
        queryParamArr.push(`/** ${param.description} */`);
      }
      queryParamArr.push(`${paramName}${param.required ? '' : '?'}: ${param.type};`);
    } else if (param.in === 'body') {
      if (bodyParamArr.length > 0) {
        console.error('不能同时存在两个body', path);
        continue;
      }

      bodyParamArr.push(`data${param.required ? '' : '?'}: ${param.type}`);
    }
  }
  if (summary) {
    arr.push(`/**`);
    arr.push(`*${summary}`);
    arr.push(`*/`);
  }
  let methodStr = '';
  methodStr += `${methodName}(`;
  if (queryParamArr.length > 0) {
    methodStr += `params:{${queryParamArr.join('\n')}}`;
    if (bodyParamArr.length > 0) {
      methodStr += ',';
    }
  }
  if (bodyParamArr.length > 0) {
    methodStr += bodyParamArr.join('');
  }

  if (responseType.startsWith(config.responseWarp)) {
    responseType = responseType.replace(config.responseWarp + 'Of', '');
    responseType = responseType.replace(/^ListOf(.*)/, (a, b) => b + '[]');
  }
  methodStr += `): Promise<${responseType}> {`;
  methodStr += `return http.${httpMethod}(baseUrl+'${path}'`;
  if (bodyParamArr.length > 0) {
    methodStr += ',data';
  }
  if (queryParamArr.length > 0) {
    methodStr += ',{params}';
  }
  methodStr += ').then(res => res.data';
  if (responseType.startsWith(config.responseWarp)) {
    methodStr += '.data';
  }
  methodStr += ')},';
  arr.push(methodStr);
}
module.exports = codegen;
