package mono.prove.kit

object Prove {
  @JvmStatic
  fun create(config: ProveConfiguration): ProveKit {

    ProveWebInterface.reset()

    return ProveKit(config)
  }
}
