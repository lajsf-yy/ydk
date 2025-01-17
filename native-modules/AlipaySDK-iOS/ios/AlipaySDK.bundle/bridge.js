!(function() {
  if (!window.AlipayJSBridge) {
    ;(window.alipayjsbridgeSetTitle = function(e) {
      ;(document.title = e), t('alipayjsbridge://setTitle?title=' + encodeURIComponent(e))
    }),
      (window.alipayjsbridgeRefresh = function() {
        t('alipayjsbridge://onRefresh?')
      }),
      (window.alipayjsbridgeBack = function() {
        t('alipayjsbridge://onBack?')
      }),
      (window.alipayjsbridgeExit = function(e) {
        t('alipayjsbridge://onExit?bsucc=' + e)
      }),
      (window.alipayjsbridgeShowBackButton = function(e) {
        t('alipayjsbridge://showBackButton?bshow=' + e)
      }),
      (window.AlipayJSBridge = {
        version: '2.0',
        addListener: function(e, i) {
          a[e] = i
        },
        hasListener: function(e) {
          if (!a[e]) return !1
          return !0
        },
        callListener: function(e, i, n) {
          var t
          n &&
            (t = function(e) {
              var i = ''
              e && (i = encodeURIComponent(JSON.stringify(e)))
              var a = 'func=h5JsFuncCallback&cbId=' + n + '&data=' + i
              o(a)
            })
          var r = a[e]
          r ? r(i, t) : console.log('AlipayJSBridge: no h5JsFunc ', e + i)
        },
        callNativeFunc: function(e, a, t) {
          var r = ''
          t && ((r = 'cb_' + i++ + '_' + new Date().getTime()), (n[r] = t))
          var d = ''
          a && (d = encodeURIComponent(JSON.stringify(a)))
          o('func=' + e + '&cbId=' + r + '&data=' + d)
        },
        callBackFromNativeFunc: function(e, i) {
          var a = n[e]
          a && (a(i), delete n[i])
        },
      })
    var e,
      i = 1,
      n = {},
      a = {}
    window.CustomEvent
      ? (e = new CustomEvent('alipayjsbridgeready'))
      : (e = document.createEvent('Event')).initEvent('alipayjsbridgeready', !0, !0),
      document.dispatchEvent(e),
      setTimeout(function() {
        if (window.AlipayJSBridgeInitArray) {
          var e = window.AlipayJSBridgeInitArray
          delete window.AlipayJSBridgeInitArray
          for (var i = 0; i < e.length; i++)
            try {
              e[i](AlipayJSBridge)
            } catch (e) {
              setTimeout(function() {
                throw e
              })
            }
        }
      }, 0)
  }
  function t(e) {
    window.webkit &&
      window.webkit.messageHandlers &&
      window.webkit.messageHandlers.MQPJSBridgeScheme &&
      window.webkit.messageHandlers.MQPJSBridgeScheme.postMessage &&
      window.webkit.messageHandlers.MQPJSBridgeScheme.postMessage(e)
  }
  function o(e) {
    t('alipayjsbridge://callNativeFunc?' + e)
  }
})()
