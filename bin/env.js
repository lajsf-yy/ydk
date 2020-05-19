const fs = require('fs');
const path = require('path');

let _ = require('lodash');
function buildEnv() {
  const rootPath = path.join(process.cwd(), 'scripts');
  const baseConfig = require(path.join(rootPath, 'env.base.json'));
  console.log(`build configuration loaded from ${rootPath}/env.base.json`);
  let config = Object.assign({}, baseConfig);
  let env = process.argv[2] || 'test';
  console.log(`merge configuration file from ${rootPath}/env.${env}.json`);
  let envConfig = require(path.join(rootPath, `env.${env}.json`));
  config = _.merge(config, envConfig);
  config.env = env;

  buildPlatform('web', config);

  buildPlatform('ios', config);

  buildPlatform('android', config);
}

function buildPlatform(platform, config) {
  let platformConfig = {};
  platformConfig = traversalNode(config, platform);

  let output = path.join(process.cwd(), config.output[platform], 'env.json');
  platformConfig = Object.assign(
    {
      desc: `自动生成${platform}配置文件，不要编辑本文件！！！`,
    },
    platformConfig,
  );

  fs.writeFileSync(output, `${JSON.stringify(platformConfig, null, 2)}`, 'utf8');
}

function traversalNode(node, platform) {
  if (typeof node !== 'object') return node;
  let { ios, web, android, ...newNode } = node;
  if (node[platform]) {
    if (typeof node[platform] !== 'object') {
      return node[platform];
    }

    Object.assign(newNode, {
      ...node[platform],
    });
  }
  for (let key in newNode) {
    newNode[key] = traversalNode(newNode[key], platform);
  }
  return newNode;
}
buildEnv();
