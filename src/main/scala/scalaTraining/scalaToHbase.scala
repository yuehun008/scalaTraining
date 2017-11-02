package scalaTraining

import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase._
import org.apache.hadoop.hbase.client.{Get, HTable, Put, Result}

import scala.language.postfixOps

object scalaToHbase {
  var conn: Connection = null
  var conf = HBaseConfiguration.create()
  conf.set("hbase.zookeeper.quorum", "192.168.137.12")
  //创建Connection
  //这里try catch 有问题,没解决
  //conn = ConnectionFactory.createConnection(conf)

  /**
    * 判断表是否存在
    */
  def tableExist(tablename: String): Unit = {
    conn = ConnectionFactory.createConnection(conf)
    var admin = conn.getAdmin
    val t_name = TableName.valueOf(tablename)
    if (admin.tableExists(t_name)) {
      println("Exist")
    }
    admin.close()
    conn.close()
  }

  /**
    * 建表方法
    */
  def createTable(tablename: String, cf: String*): Unit = {
    conn = ConnectionFactory.createConnection(conf)
    var admin = conn.getAdmin
    val t_name = TableName.valueOf(tablename)
    if (admin.tableExists(t_name)) {
      println(s"表 $t_name 已经存在!")
    } else {
      val tableDesc: HTableDescriptor = new HTableDescriptor(t_name)
      for (column_family <- cf) {
        tableDesc.addFamily(new HColumnDescriptor(column_family))
      }
      admin.createTable(tableDesc)
      println(s"$t_name 创建成功")
    }
    admin.close()
    conn.close()
  }

  /**
    * 删除表
    */
  def deleteTable(tableName: String): Unit = {
    conn = ConnectionFactory.createConnection(conf)
    var admin = conn.getAdmin
    val t_name = TableName.valueOf(tableName)
    if (admin.tableExists(t_name)) {
      admin.disableTable(t_name)
      admin.deleteTable(t_name)
      println(s"$t_name 已经删除成功！")
    } else {
      println(s"表 $t_name 不存在，请重新输入!")
    }
    admin.close()
    conn.close()
  }

  /**
    * 添加一个cell数据
    */
  def addRow(tableName: String, rowkey: String, cf: String, col: String, value: String): Unit = {
    conn = ConnectionFactory.createConnection(conf)
    var admin = conn.getAdmin
    println("准备插入数据")
    val table: HTable = new HTable(conf, tableName)
    val put: Put = new Put(Bytes.toBytes(rowkey))
    put.add(Bytes.toBytes(cf), Bytes.toBytes(col), Bytes.toBytes(value))
    table.put(put)
    println("插入成功")
    admin.close()
    conn.close()
  }

  /**
    * 删除一个 cell 数据
    */
  def delRow(tableName: String, rowkey: String, cf: String, col: String): Unit = {
    conn = ConnectionFactory.createConnection(conf)
    var admin = conn.getAdmin
    val table = new HTable(conf, tableName)
    val del = new Delete(Bytes.toBytes(rowkey))
    if (col != null) {
      del.addColumn(Bytes.toBytes(cf), Bytes.toBytes(col))
    } else if (cf != null) {
      del.addFamily(Bytes.toBytes(cf))
    }
    table.delete(del)
    admin.close()
    conn.close()
  }

  /**
    * 删除一个列族数据
    */
  def delRow(tableName: String, rowkey: String, cf: String) {
    return delRow(tableName, rowkey, cf, null)
  }

  /**
    * 删除一行数据
    */
  def delRow(tableName: String, rowkey: String) {
    return delRow(tableName, rowkey, null, null)
  }

  //  /**
  //    * get 显示指定列,如需要获取，则可以通过修改代码返回值
  //    * 这里列输入采用数组，可以返回多列
  //    *
  //    * @param col 所有列名
  //    */
  //  def getRow_cell(tableName: String, rowkey: String, cf: String, col: Array[String]): Unit = {
  //    var result: AnyRef = null
  //    val t_name = TableName.valueOf(tableName)
  //    val table = conn.getTable(t_name)
  //    val get = new Get(Bytes.toBytes(rowkey))
  //    val HBasaeRow = table.get(get)
  //    if (HBasaeRow != null && !HBasaeRow.isEmpty) {
  //      result = col.map(c => {
  //        (tableName + "." + c + " ", Bytes.toString(HBasaeRow.getValue(Bytes.toBytes(cf), Bytes.toBytes(c))))
  //      })
  //    } else {
  //      result = col.map(c => {
  //        (tableName + "." + c + "." + cf, "null")
  //      })
  //    }
  //    //这一句为啥要加？？？
  //    val result1 = result.asInstanceOf[Array[(String, String)]]
  //    //打印显示
  //    for (r <- result1) {
  //      println(r)
  //    }
  //    //admin.close()
  //    table.close()
  //  }

  /**
    * 读取一个cell值,这个方式更容易理解
    */
  def getRowCell(tableName: String, rowkey: String, cf: String, col: String*): Unit = {
    conn = ConnectionFactory.createConnection(conf)
    val t_name = TableName.valueOf(tableName)
    val table: Table = conn.getTable(t_name)
    val get: Get = new Get(Bytes.toBytes(rowkey))
    for (c <- col) {
      get.addColumn(Bytes.toBytes(cf), Bytes.toBytes(c))
    }
    val result = table.get(get)
    for (r <- result.raw()) {
      val r_rowkey = new String(r.getRow)
      val r_rowCF = new String(r.getFamily)
      val r_rowcol = new String(r.getQualifier)
      val r_rowvalue = new String(r.getValue)
      println(r_rowkey + "  " + r_rowCF + "  " + r_rowcol + "  " + r_rowvalue)
    }
    table.close()
    conn.close()
  }


  /**
    * 读取一个列族所有信息
    */
  def getRowCF(tableName: String, rowkey: String, cf: String): Unit = {
    conn = ConnectionFactory.createConnection(conf)
    val t_name = TableName.valueOf(tableName)
    val table: Table = conn.getTable(t_name)
    val get: Get = new Get(Bytes.toBytes(rowkey))
    get.addFamily(Bytes.toBytes(cf))
    val result = table.get(get)
    for (r <- result.raw()) {
      val r_rowkey = new String(r.getRow)
      val r_rowCF = new String(r.getFamily)
      val r_rowcol = new String(r.getQualifier)
      val r_rowvalue = new String(r.getValue)
      println(r_rowkey + "  " + r_rowCF + "  " + r_rowcol + "  " + r_rowvalue)
    }
    table.close()
    conn.close()
  }

  /**
    * 读取一条信息,一个rowkey
    */
  def getRow(tableName: String, rowkey: String): Unit = {
    conn = ConnectionFactory.createConnection(conf)
    val t_name = TableName.valueOf(tableName)
    val table = conn.getTable(t_name)
    val get: Get = new Get(Bytes.toBytes(rowkey))
    val result = table.get(get)
    for (row <- result.raw()) {
      val rowid = new String(row.getRow)
      val cfid = new String(row.getFamily)
      val colid = new String(row.getQualifier)
      val valueid = new String(row.getValue)
      println(rowid + "  " + cfid + "  " + colid + "  " + valueid)
    }
    table.close()
    conn.close()
  }

  /**
    * 入口
    */
  def main(args: Array[String]) {
    //测试语句
    //tableExist("test1")
    createTable("scala_t1", "cf1", "cf2", "cf3")
    //deleteTable("scala_t1")
    //新增测试数据
    //    addRow("scala_t1", "001", "cf1", "name", "wxk")
    //    addRow("scala_t1", "001", "cf1", "age", "33")
    //    addRow("scala_t1", "001", "cf2", "job", "IT")
    //    addRow("scala_t1", "001", "cf2", "sal", "8k")
    //    addRow("scala_t1", "002", "cf1", "name", "panda")
    //    addRow("scala_t1", "002", "cf2", "sal", "1w")

    //delRow("scala_t1","002")

    //getRow_cell("scala_t1", "001", "cf1", Array("age"))
    //getRowCF("scala_t1","001","cf1")
    //getRowCell("scala_t1","001","cf1","name","age")

    //getRow("scala_t1", "001")

  }

}
