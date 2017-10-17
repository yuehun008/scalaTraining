package scalaTraining

import java.sql.{Connection, DriverManager, ResultSet, ResultSetMetaData}

import scala.collection.mutable.ArrayBuffer

object Scala2Msql_JDBC {
  def main(args: Array[String]) {
    //connect to database named "mysql" on "192.168.137.12"
    val driver = "com.mysql.jdbc.Driver"
    val url = "jdbc:mysql://192.168.137.12:3306/sqoop"
    val username = "root"
    val password = "password"
    var connection: Connection = null

    try {
      Class.forName(driver)
      connection = DriverManager.getConnection(url, username, password)
      val statement = connection.createStatement()

      //需要查询的sql，这里需要输入sql
      println("Please enter your sql :")
      val line = Console.readLine()
      println("Thanks,you just typed :  " + line)
      println("The result is :")

      val resultSet = statement.executeQuery(line)

      //获取结果列数
      val metaData = resultSet.getMetaData
      val col = metaData.getColumnCount
      //获取结果行数
      resultSet.last()
      val row = resultSet.getRow()
      resultSet.beforeFirst()

      //构建二位数组
      var matrix = Array.ofDim[String](row, col)
      //将读取的值放入二维数组
      var c = 0
      var r = 0
      while (resultSet.next()) {
        for (ele <- 1 to col) {
          matrix(r)(c) = resultSet.getString(ele)
          c = c + 1
        }
        r = r + 1
        c = 0
      }
      //打印二维数组，分隔符为 \t
      for (i <- matrix) {
        println(i.mkString("\t"))
      }
    } catch {
      case e => e.printStackTrace
      //case _: Throwable => println("Error")
    }
    connection.close()
    //结束
  }
}
