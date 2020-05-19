module.exports = {
  apis: [
    {
      name: 'appinfo',
      url: 'http://home.mst.casicloud.com/1/appinfo/swagger/swagger-ui.html',
    },
    {
      name: 'approval',
      url: 'http://home.mst.casicloud.com/1/approval/swagger/swagger-ui.html',
    },
    {
      name: 'appVersion',
      url: 'http://home.mst.casicloud.com/1/app_version/swagger/swagger-ui.html',
    },
    {
      name: 'buy',
      url: 'http://home.mst.casicloud.com/1/buy/swagger/swagger-ui.html',
    },
    {
      name: 'contract',
      url: 'http://home.mst.casicloud.com/1/contract/swagger/swagger-ui.html',
    },
    {
      name: 'order',
      url: 'http://home.mst.casicloud.com/1/order/swagger/swagger-ui.html',
    },
    {
      name: 'permission',
      url: 'http://home.mst.casicloud.com/1/permission/swagger/swagger-ui.html',
    },
    {
      name: 'quote',
      url: 'http://home.mst.casicloud.com/1/quote/swagger/swagger-ui.html',
    },
    {
      name: 'cm',
      url: 'http://cm.mst.casicloud.com/2/cm-cloudmarketing/swagger/swagger-ui.html',
    },
    {
      name: 'auth',
      url: 'http://auth.mst.casicloud.com/1/user/swagger/swagger-ui.html',
    },
    {
      name: 'inquiry',
      url: 'http://home.mst.casicloud.com/1/inquiry/swagger/v2/swagger-ui.html',
    },
  ],

  mapTypes: { Timestamp: 'number', Int64: 'string' },
  codegenType: 'ts',
  pathSplitIndex: 2,
  responseWarp: 'ApiResult',
};
