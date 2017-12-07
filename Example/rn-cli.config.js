const path = require('path');

// As the metro bundler does not support linking correctly, we add additional
// search path queries to all modules.
// See https://github.com/facebook/metro-bundler/issues/68
const extraNodeModulesGetter = {
  get: (target, name) => path.join(process.cwd(), `node_modules/${name}`),
};

module.exports = {
  extraNodeModules: new Proxy({}, extraNodeModulesGetter),
};
