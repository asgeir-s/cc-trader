package com.cctrader

import java.util.Date

import com.cctrader.data.CurrencyPair._
import com.cctrader.data.Exchange._
import com.cctrader.data.Granularity.Granularity

/**
 *
 */
case class RequestData(dateNow: Date,
                       numberOfHistoricalPoints: Int,
                       granularity: Granularity,
                       currencyPair: CurrencyPair,
                       exchange: Exchange)

case class DataReady(fromDate: Date, toDate: Date)
