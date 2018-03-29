package jsky.app.ot.viewer.planner

import edu.gemini.pot.sp.SPComponentType
import edu.gemini.pot.sp.SPComponentType._
import java.awt.datatransfer.DataFlavor;

/** This class is used to know if any Observation is SX or DX */
sealed trait Side {
  /** all instrument types valid for one side of the telescope */
  val types: Set[SPComponentType]

  /** SX or DX */
  val title: String

  /** Drag and drop data for SX and DX sides are each made with artificial flavors */
  val flavor: DataFlavor

  /** Returns true if the observation instrument is on this side of the telescope */
  def apply(t: SPComponentType): Boolean = types.contains(t)

}

/** Left side of the LBT */
object Sx extends Side {
  override val title = "SX"
  override val flavor = new DataFlavor(getClass, "SxDataFlavor");
  override val types = Set(
    INSTRUMENT_LBCBLUE,
    INSTRUMENT_LUCI1,
    INSTRUMENT_MODS1,
    INSTRUMENT_LUCIFRATERNAL)
}

/** Right side of the LBT */
object Dx extends Side {
  override val title = "DX"
  override val flavor = new DataFlavor(getClass, "DxDataFlavor");
  override val types = Set(
    INSTRUMENT_LBCRED, 
    INSTRUMENT_LUCI2, 
    INSTRUMENT_MODS2,
    INSTRUMENT_LUCIFRATERNAL)
}

