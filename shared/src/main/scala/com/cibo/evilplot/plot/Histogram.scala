package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, Extent}
import com.cibo.evilplot.numeric.{Bounds, Point}
import com.cibo.evilplot.plot.renderers.{BarRenderer, PlotRenderer}

object Histogram {

  val defaultBinCount: Int = 20

  case class HistogramRenderer(
    barRenderer: BarRenderer,
    binWidth: Double,
    spacing: Double
  ) extends PlotRenderer[Seq[Point]] {
    def render(plot: Plot[Seq[Point]], plotExtent: Extent): Drawable = {
      val xtransformer = plot.xtransform(plot, plotExtent)
      val ytransformer = plot.ytransform(plot, plotExtent)

      plot.data.map { point =>
        val x = xtransformer(point.x)
        val barWidth = xtransformer(point.x + binWidth) - x
        val y = ytransformer(point.y)
        val barHeight = plotExtent.height - y
        barRenderer.render(Bar(point.y), Extent(barWidth, barHeight), 0).translate(x = x, y = y)
      }.group
    }
  }

  def apply(
    values: Seq[Double],
    bins: Int = defaultBinCount,
    barRenderer: BarRenderer = BarRenderer.default(),
    spacing: Double = BarChart.defaultSpacing,
    boundBuffer: Double = BarChart.defaultBoundBuffer
  ): Plot[Seq[Point]] = {
    require(bins > 0, "must have at least one bin")
    val (minValue, maxValue) = (values.min, values.max)
    val binWidth = (maxValue - minValue) / bins
    val grouped = values.groupBy { value => math.min(((value - minValue) / binWidth).toInt, bins - 1) }
    val points = (0 until bins).map { i =>
      val count = grouped.get(i).map(_.size).getOrElse(0)
      val x = i * binWidth + minValue
      Point(x, count)
    }
    Plot[Seq[Point]](
      data = points,
      xbounds = Bounds(minValue, maxValue),
      ybounds = Bounds(0, points.maxBy(_.y).y * (1.0 + boundBuffer)),
      renderer = HistogramRenderer(barRenderer, binWidth, spacing)
    )
  }
}
