val scalafmtAliases: Seq[Def.Setting[State => State]] =
  alias("checkFmt", "scalafmtCheckAll", "scalafmtSbtCheck") ++
    alias("runFmt", "scalafmtAll", "scalafmtSbt")

val scalafixAliases: Seq[Def.Setting[State => State]] =
  alias("checkFix", "scalafixAll --check OrganizeImports", "scalafixAll --check") ++
    alias("runFix", "scalafixAll OrganizeImports", "scalafixAll")

val scalalintAliases: Seq[Def.Setting[State => State]] =
  alias("checkLint", "checkFmt", "checkFix") ++
    alias("runLint", "runFmt", "runFix")

def taskList(tasks: String*): String                                      = tasks.mkString("; ")
def alias(name: String, tasks: String*): Seq[Def.Setting[State => State]] = addCommandAlias(name, taskList(tasks*))

scalafmtAliases ++ scalafixAliases ++ scalalintAliases
