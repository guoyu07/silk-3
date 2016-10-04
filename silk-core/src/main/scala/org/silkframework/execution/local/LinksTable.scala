package org.silkframework.execution.local

import org.silkframework.entity.{Entity, EntitySchema, Link, Path}
import org.silkframework.util.Uri

case class LinksTable(links: Seq[Link], linkType: Uri) extends EntityTable {

  val entitySchema = LinksTable.linkEntitySchema

  val entities = {
    for (link <- links) yield
      new Entity(
        uri = link.source,
        values = IndexedSeq(Seq(link.target), Seq(link.confidence.getOrElse(0.0).toString)),
        desc = entitySchema
      )
  }

}

object LinksTable {

  val linkEntitySchema = EntitySchema("", IndexedSeq(Path("targetUri"), Path("confidence")))

}