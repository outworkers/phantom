/*
  /**
   * Produces a [[com.twitter.concurrent.Spool]] of [R]ows
   * A spool is both lazily constructed and consumed, suitable for large
   * collections when using twitter futures.
 *
   * @param session The cassandra session in use.
   * @return A Spool of R.
   */
  def fetchSpool()(implicit session: Session, keySpace: KeySpace): TwitterFuture[Spool[R]] = {
    execute() flatMap {
      resultSet => ResultSpool.spool(resultSet).map(spool => spool map fromRow)
    }
  }

 */