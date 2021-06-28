package com.book.manager.infrastructure.database.dbunit

import com.github.springtestdbunit.dataset.AbstractDataSetLoader
import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.csv.CsvDataSet
import org.springframework.core.io.Resource

class CsvDataSetLoader : AbstractDataSetLoader() {
    override fun createDataSet(resource: Resource?): IDataSet = CsvDataSet(resource!!.file)
}