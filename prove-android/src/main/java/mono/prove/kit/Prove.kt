package mono.prove.kit

object Prove {
  fun create(config: ProveConfiguration): ProveKit {

    ProveWebInterface.reset()

    return ProveKit(config)
  }
}
