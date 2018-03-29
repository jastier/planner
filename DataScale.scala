package jsky.app.ot.viewer.planner


/** A class for determining the scale factor for sequence displays */
object DataScale {

  /** map a list of sequences into a list of their steps */
  def seqs(presets: List[Preset]):List[Sequence] = presets.map(p => p.seqs).toList.flatten

  /** map a list of sequences into a list of their steps */
  def steps(seqs: List[Sequence]):List[Step] = seqs.map(s => s.steps).toList.flatten

  /** return the shortest step in the list of steps */
  def shortestStep(l: List[Step]): Int = if(l.isEmpty) 0 else l.map(step => step.seconds).min

  /** Pass in a list of sequencs and a minimum height, and a rubber chicken will come back */
  def apply(min: Double, presets: List[Preset]): Double = _3(min, seqs(presets))

  def _3(min: Double, seqs: List[Sequence]): Double = _2(min, steps(seqs))
  def _2(min: Double, steps: List[Step]): Double = _1(min, shortestStep(steps))
  def _1(min: Double, shortest: Int): Double = if (shortest > 0) min/shortest else min
}

