package ems.gcn.layers

import com.intel.analytics.bigdl.nn.abstractnn.AbstractModule
import com.intel.analytics.bigdl.tensor.{SparseTensorMath, Tensor}
import com.intel.analytics.bigdl.tensor.TensorNumericMath.TensorNumeric
import org.apache.log4j.Logger

import scala.reflect.ClassTag

class GraphConvolution[T: ClassTag](val adjMatrix: Tensor[T], val batchSize: Int, val featuresNumber: Int)(
    implicit ev: TensorNumeric[T]
) extends AbstractModule[Tensor[T], Tensor[T], T] {

  @transient lazy val logger = Logger.getLogger(getClass)

  override def updateOutput(input: Tensor[T]): Tensor[T] = {
    output.resize(batchSize, featuresNumber).zero()
    logger.debug(s"Input tensor ${input}")
    SparseTensorMath.addmm(
      output,
      ev.zero,
      Tensor(Array(adjMatrix.size(1), input.size(2))).zero(),
      ev.one,
      adjMatrix,
      input
    )
    output
  }

  override def updateGradInput(input: Tensor[T], gradOutput: Tensor[T]): Tensor[T] = {
    gradInput.resizeAs(gradOutput).copy(gradOutput)
    gradInput
  }

}

object GraphConvolution {
  def apply[@specialized(Float, Double) T: ClassTag](adjMatrix: Tensor[T], batchSize: Int, featuresNumber: Int)(
      implicit ev: TensorNumeric[T]
  ): GraphConvolution[T] = {
    new GraphConvolution[T](adjMatrix, batchSize, featuresNumber)
  }
}
