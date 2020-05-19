package com.yryz.network.http.transform

import com.yryz.network.http.model.BaseModel
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody

/**
 * 扩展函数
 *
 * 实现数据转换的链式调用
 *
 */
/**
 *
 * example
 *
 * 在 kotlin 中调用
 *
 *  HttpClient.getClient().get(url,map).asObject(Boolean::class.java).composeMain()
 *
 * 在 java 中调用
 *
 *  YDKObservableKt.composeMain(YDKObservableKt.asObject(HttpClient.get(url,map), Boolean.class))
 *
 */

/**
 * 转化为普通对象
 *@param type 需要转化的数据源  clazz
 *
 *@return Observable<BaseModel<T>> 标准数据返回
 *
 */
fun <T> Observable<ResponseBody>.asObject(type: Class<T>): Observable<BaseModel<T>> {

    return this.map(TransFormFunction1(type))
}

/**
 * 转化为普通对象集合
 * @param type 需要转化的数据源  clazz
 *
 * @return Observable<BaseModel<T>> 标准数据返回
 */
fun <T> Observable<ResponseBody>.asList(type: Class<T>): Observable<BaseModel<List<T>>> {

    return this.map(TransFormFunction2(type))
}

/**
 *
 *@return Observable<BaseModel<Boolean>> 标准数据返回
 */
fun Observable<ResponseBody>.asBoolean(): Observable<BaseModel<Boolean>> {

    return asObject(Boolean::class.java)
}


/**
 *
 *@return Observable<BaseModel<Long>> 数据返回
 */
fun Observable<ResponseBody>.asLong(): Observable<BaseModel<Long>> {

    return asObject(Long::class.java)
}


/**
 *
 *@return Observable<BaseModel<Boolean>> 数据返回
 */
fun Observable<ResponseBody>.asInt(): Observable<BaseModel<Int>> {

    return asObject(Int::class.java)
}

/**
 *
 *@return Observable<BaseModel<Double>> 数据返回
 */
fun Observable<ResponseBody>.asDouble(): Observable<BaseModel<Double>> {

    return asObject(Double::class.java)
}

/**
 * 切换线程
 */
fun <T> Observable<BaseModel<T>>.composeMain(): Observable<BaseModel<T>> {

    return this.compose(applySchedulers())

}

/**
 * 切换线程
 */
fun <T> Observable<T>.composeMain2(): Observable<T> {

    return this.compose(applySchedulers())

}

/**
 * 切换线程
 */
fun <T> applySchedulers(): ObservableTransformer<T, T> {

    return ObservableTransformer { upstream ->
        upstream.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

}