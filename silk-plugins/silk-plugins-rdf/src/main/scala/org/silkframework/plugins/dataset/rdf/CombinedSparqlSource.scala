package org.silkframework.plugins.dataset.rdf

import org.silkframework.dataset.DataSource
import org.silkframework.entity.{Entity, EntitySchema, Path}
import org.silkframework.util.Uri

/**
  * A helper data source to combine several SPARQL sources in order to retrieve entities from them.
  * The sources are not merged, but instead are queries one by one.
  */
case class CombinedSparqlSource(sparqlSources: SparqlSource*) extends DataSource {
  /**
    * Retrieves entities from this source which satisfy a specific entity schema.
    *
    * @param entitySchema The entity schema
    * @param limit        Limits the maximum number of retrieved entities
    * @return A Traversable over the entities. The evaluation of the Traversable may be non-strict.
    */
  override def retrieve(entitySchema: EntitySchema, limit: Option[Int]): Traversable[Entity] = {
    new Traversable[Entity] {
      override def foreach[U](f: (Entity) => U): Unit = {
        for (sparqlSource <- sparqlSources;
             entity <- sparqlSource.retrieve(entitySchema, limit)) {
          f(entity)
        }
      }
    }
  }

  /**
    * Retrieves a list of entities from this source.
    *
    * @param entitySchema The entity schema
    * @param entities     The URIs of the entities to be retrieved.
    * @return A Traversable over the entities. The evaluation of the Traversable may be non-strict.
    */
  override def retrieveByUri(entitySchema: EntitySchema, entities: Seq[Uri]): Seq[Entity] = {
    val results = for (sparqlSource <- sparqlSources) yield {
      sparqlSource.retrieveByUri(entitySchema, entities)
    }
    results.flatten
  }

  override def retrieveTypes(limit: Option[Int]): Traversable[(String, Double)] = Traversable.empty

  override def retrievePaths(typeUri: Uri, depth: Int, limit: Option[Int]): IndexedSeq[Path] = IndexedSeq.empty
}
