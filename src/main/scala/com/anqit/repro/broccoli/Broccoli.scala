package com.anqit.repro.broccoli

class Broccoli {
    // types from "slick"
    abstract class Table[E]
    type TableQuery[W <: Table[_]] = List[W] // not actually a list, but need a concrete type constructor to demonstrate the issue
    object TableQuery {
        def apply[W <: Table[_]]: TableQuery[W] = List[W]()
    }

    // local models
    trait BaseEntity
    case class SubEntityA() extends BaseEntity
    case class SubEntityB() extends BaseEntity

    // local usage of "slick" classes
    trait BaseSchema[E <: BaseEntity] {
        // provides common functionality
        abstract class BaseTableImpl[E] extends Table[E]

        def wrapper: TableQuery[_ <: BaseTableImpl[E]]
    }

    // functionality specific to SubEntityA
    trait SchemaA extends BaseSchema[SubEntityA] {
        class TableA extends BaseTableImpl[SubEntityA]

        // this definition compiles fine without a type annotation
        val queryA = TableQuery[TableA]
        def wrapper = queryA
    }

    abstract class SchemaB(val schemaA: SchemaA) extends BaseSchema[SubEntityB] {
        class TableB extends BaseTableImpl[SubEntityB] {
            // uses SchemaA's queryA to make a FK
            def as = schemaA.queryA
        }

        val queryB = TableQuery[TableB]
        override def wrapper: TableQuery[TableB] = queryB
    }

    abstract class BaseDao[E <: BaseEntity, S <: BaseSchema[E]] (val s: S) {
        def getWrapper = s.wrapper
    }

    class DaoA(s: SchemaA) extends BaseDao[SubEntityA, SchemaA](s) {
        def doSomething = getWrapper
    }

    class DaoB(s: SchemaB) extends BaseDao[SubEntityB, SchemaB](s) {

    }
}
