package com.github.manosbatsis.corbeans.cordapp.convert

import com.github.manosbatsis.corbeans.cordapp.model.FlowInput
import com.github.manosbatsis.corbeans.cordapp.model.TransactionItems

/** Converts a source object to a [TransactionItems] instance */
interface ToTransactionItemsConverter<T: FlowInput>{
    /** Convert the source object */
    fun convert(source: T): TransactionItems
}
