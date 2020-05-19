package ydk.mqtt


/**
 * 请求回调接口
 */
interface RequestCallback<T> {
    /**
     * 操作成功
     * @param param 操作结果
     */
    fun onSuccess(param: T? = null)

    /**
     * 操作失败
     * @param code 错误码。
     */
    fun onFailed(code: Int)

    /**
     * 操作过程中发生异常
     * @param exception 异常详情
     */
    fun onException(exception: Throwable?)
}

class SimpleRequestCallback<T> : RequestCallback<T> {
    override fun onSuccess(param: T?) {
    }

    override fun onFailed(code: Int) {
    }

    override fun onException(exception: Throwable?) {
    }

}