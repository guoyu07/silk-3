package org.silkframework.plugins.dataset.rdf

import java.util.logging.{Level, Logger}

import org.silkframework.dataset.{DataSource, PeakDataSource}
import org.silkframework.dataset.rdf.{SparqlEndpoint, SparqlParams}
import org.silkframework.entity.rdf.SparqlRestriction
import org.silkframework.entity.{Entity, EntitySchema, Path}
import org.silkframework.plugins.dataset.rdf.sparql._
import org.silkframework.util.Uri

/**
 * A source for reading from SPARQL endpoints.
 */
class SparqlSource(params: SparqlParams, val sparqlEndpoint: SparqlEndpoint) extends DataSource with PeakDataSource {

  private val log = Logger.getLogger(classOf[SparqlSource].getName)

  private val entityUris: Seq[String] = params.entityRestriction

  override def retrieve(entitySchema: EntitySchema, limit: Option[Int] = None): Traversable[Entity] = {
    val entityRetriever = EntityRetriever(sparqlEndpoint, params.strategy, params.pageSize, params.graph, params.useOrderBy)
    entityRetriever.retrieve(entitySchema, entityUris.map(Uri(_)), None)
  }

  override def retrieveByUri(entitySchema: EntitySchema, entities: Seq[Uri]): Seq[Entity] = {
    if(entities.isEmpty) {
      Seq.empty
    } else {
      val entityRetriever = EntityRetriever(sparqlEndpoint, params.strategy, params.pageSize, params.graph, params.useOrderBy)
      entityRetriever.retrieve(entitySchema, entities, None).toSeq
    }
  }

  override def retrievePaths(t: Uri, depth: Int = 1, limit: Option[Int] = None): IndexedSeq[Path] = {
    val restrictions = SparqlRestriction.forType(t)

    //Create an endpoint which fails after 3 retries
    val failFastEndpoint = sparqlEndpoint.withSparqlParams(params.copy(retryCount = 3, retryPause = 1000))

    try {
      SparqlAggregatePathsCollector(failFastEndpoint, params.graph, restrictions, limit)
    } catch {
      case ex: Exception =>
        log.log(Level.INFO, "Failed to retrieve the most frequent paths using a SPARQL 1.1 aggregation query. Falling back to sampling.", ex)
        SparqlSamplePathsCollector(sparqlEndpoint, params.graph, restrictions, limit).toIndexedSeq
    }
  }

  override def retrieveTypes(limit: Option[Int]): Traversable[(String, Double)] = {
    SparqlTypesCollector(sparqlEndpoint, params.graph, limit)
  }

  override def toString = sparqlEndpoint.toString
}