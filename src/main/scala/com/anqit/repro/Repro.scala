package com.anqit.repro

class Repro {
    // types from external library
    abstract class Table[E]
    type TableQuery[W <: Table[_]] = List[W] // not actually a list, but need a concrete type constructor to deomonstrate the issue
    object TableQuery {
        def apply[W <: Table[_]]: TableQuery[W] = List[W]()
    }

    // local models
    trait BaseEntity
    trait SubEntityA extends BaseEntity
    trait SubEntityB extends BaseEntity

    // local usage of library classes
    trait BaseSchema[E <: BaseEntity] {
        // provides common functionality
        abstract class BaseTableImpl[E] extends Table[E]

        def wrapper: TableQuery[_ <: BaseTableImpl[E]]
    }

    // functionality specific to SubEntityA
    trait SchemaA extends BaseSchema[SubEntityA] {
        class TableA extends BaseTableImpl[SubEntityA]

        // this definition compiles fine without a type annotation
        def wrapper = TableQuery[TableA]
    }

    // functionality specific to SubEntityB that depends on SubA
    trait SchemaB extends BaseSchema[SubEntityB] { self: SchemaA =>
        class TableB extends BaseTableImpl[SubEntityB]

        /*
            attempting to define wrapper here without a type annotation results in the following compilation error:
            def wrapper = Wrapper[WrappedB]
                type mismatch;
                [error]  found   : Repro.this.Wrapper[SubB.this.WrappedB]
                [error]     (which expands to)  List[SubB.this.WrappedB]
                [error]  required: Repro.this.Wrapper[_ <: SubB.this.BaseWrapMeImpl[_1]]
                [error]     (which expands to)  List[_ <: SubB.this.BaseWrapMeImpl[_1]]
                [error]         def wrapper = Wrapper[WrappedB]
                [error]                              ^

            it does, however, compile if defined with an explicit type annotation as below
        */

        def wrapper: TableQuery[TableB] = TableQuery[TableB]
    }

    trait BaseDao[E <: BaseEntity] { self: BaseSchema[E] => }

    // now, the actual injection of the traits
    class DaoA extends SchemaA
        with BaseDao[SubEntityA]
    // so far so good...

    class DaoB extends SchemaA
        with SchemaB
        with BaseDao[SubEntityB] // blargh! failure! :

    /*
         illegal inheritance;
        [error]  self-type Repro.this.DaoB does not conform to Repro.this.BaseDao[Repro.this.SubEntityB]'s selftype Repro.this.BaseDao[Repro.this.SubEntityB] with Repro.this.BaseSchema[Repro.this.SubEntityB]
        [error]         with BaseDao[SubEntityB]
        [error]              ^
     */
}
