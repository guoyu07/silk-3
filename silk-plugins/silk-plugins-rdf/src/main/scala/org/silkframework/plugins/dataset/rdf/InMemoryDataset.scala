package org.silkframework.plugins.dataset.rdf

import org.apache.jena.rdf.model.ModelFactory
import org.silkframework.dataset._
import org.silkframework.dataset.rdf.{RdfDataset, SparqlEndpoint, SparqlParams}
import org.silkframework.plugins.dataset.rdf.endpoint.JenaModelEndpoint
import org.silkframework.runtime.plugin.{Param, Plugin}

@Plugin(id = "inMemory", label = "in-memory", description = "A Dataset that holds all data in-memory.")
case class InMemoryDataset(@Param(label = "Clear graph before workflow execution",
                                  value = "If set to true this will clear this dataset before it is used in a workflow execution.")
                           clearGraphBeforeExecution: Boolean = true) extends RdfDataset with TripleSinkDataset {

  private val model = ModelFactory.createDefaultModel()

  override val sparqlEndpoint: SparqlEndpoint = new JenaModelEndpoint(model)

  /**
    * Returns a data source for reading entities from the data set.
    */
  override val source: DataSource = new SparqlSource(SparqlParams(), sparqlEndpoint)

  /**
    * Returns a entity sink for writing entities to the data set.
    */
  override val entitySink: EntitySink = new SparqlSink(SparqlParams(), sparqlEndpoint, dropGraphOnClear = clearGraphBeforeExecution)

  /**
    * Returns a link sink for writing entity links to the data set.
    */
  override val linkSink: LinkSink = new SparqlSink(SparqlParams(), sparqlEndpoint, dropGraphOnClear = clearGraphBeforeExecution)

  override def tripleSink: TripleSink = new SparqlSink(SparqlParams(), sparqlEndpoint, dropGraphOnClear = clearGraphBeforeExecution)
}
