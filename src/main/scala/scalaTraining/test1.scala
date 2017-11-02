package scalaTraining

object test1 {
  def main(args: Array[String]): Unit = {
    val arr = Array(1, 2, 3, 4, 5)
    val a_map = arr.map(c => {
      (c,c+"!!!!")
    })
    val a_map_as = a_map.asInstanceOf[Array[(String,String)]]
    for(a <- a_map_as){
      println(a)
    }

  }

}
