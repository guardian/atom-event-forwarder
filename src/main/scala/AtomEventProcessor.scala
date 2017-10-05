import com.gu.contentatom.thrift.{Atom, AtomType}
import com.gu.crier.model.event.v1.EventType


object AtomEventProcessor {
  def process(atom: Atom, eventType: EventType):Boolean = {
    println(s"Got atom title ${atom.title} event $eventType")
    atom.atomType match {
      case AtomType.Media=>
        println("Got a media atom!")
        true
      case _=>
        println("Got some other kinda atom, bored, going home.")
        false
    }
  }
}
