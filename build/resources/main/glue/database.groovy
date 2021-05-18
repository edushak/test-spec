
import groovy.sql.Sql

import static cucumber.api.groovy.EN.*

When(~/^I execute statements on database (.*):$/) { String dbAlias, String query ->
    Map config = binding['databases'][dbAlias]
    Sql sql = Sql.newInstance(config)
    sql.execute(query)
}

When(~/^I capture into (.*) result of query on database (.*):$/) { String datasetName, String dbAlias, String query ->
    Map config = binding['databases'][noQuotes(dbAlias)]
    Sql sql = Sql.newInstance(config)
    binding[noQuotes(datasetName)] = sql.rows(query)
}

//When(~/^I capture into (.*) result of query:$/) { String datasetName, String query ->
//
//}
//
//When(~/^I capture result of query (.*) into (.*)$/) { String queryAlias, String datasetName ->
//
//}
