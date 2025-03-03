const { defineConfig } = require('@vue/cli-service');

module.exports = defineConfig({
  transpileDependencies: true,
  chainWebpack: config => {
    config.plugin('define').tap(definitions => {
      Object.assign(definitions[0]['process.env'], {
        NODE_HOST: '"http://localhost:8888"',
      });
      return definitions;
    });
  },
  devServer: {
    client: {
      overlay: {
        warnings: false, // 关闭警告覆盖层
        errors: false,   // 关闭错误覆盖层
        runtimeErrors: false, // 关闭运行时错误覆盖层
      },
    },
  },
});