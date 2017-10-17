package scalaTraining


import java.util
import java.sql.{Connection, DriverManager}

object ScalaToMySQL_JDBCs {

  /**
    * mysql 数据库连接池
    */
  object MySqlPool {
    private val max = 8 //连接池连接数
    private val connectionNum = 10 //每次产生连接数
    private var conNum = 0 //当前连接数
    private val pool = new util.LinkedList[Connection]()


    //连接池
    def getJdbcConn(): Connection = {
      //同步代码块
      AnyRef.synchronized({
        if (pool.isEmpty) {
          //加载驱动
          preGetConn()
          for (i <- 1 to connectionNum) {
            val conn = DriverManager.getConnection("jdbc:mysql://192.168.137.12:3306/sqoop", "root", "password")
            pool.push(conn)
            conNum += 1
          }
        }
        pool.poll()
      })
    }

    //释放连接
    def releaseConn(conn: Connection): Unit = {
      pool.push(conn)
    }

    //加载驱动
    private def preGetConn(): Unit = {
      //控制加载
      if (conNum < max && !pool.isEmpty) {
        println("Jdbc Pool has no connection now, please wait a moments!")
        Thread.sleep(2000)
        preGetConn
      }
      else Class.forName("com.mysql.jdbc.Driver")
    }
  }

  /**
    * 测试连接
    */
  def main(args: Array[String]) {
          for (x <- 1 to 20) {
            val conn = MySqlPool.getJdbcConn()
            println("当前连接：" + x + conn)
            if (x == 6) {
              println("释放的是：" + x + " " + conn)
              MySqlPool.releaseConn(conn)
            }
          }
    val conn = MySqlPool.getJdbcConn()



  }
}
