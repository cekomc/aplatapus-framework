class Defs {

    public static final def Repo =
            'https://github.com/cekomc/aplatapus-framework.git'

    public static final def QA_GROUP = ['email']

    public static final def EMAIL_ALERT = QA_GROUP

    public static final def SLACK_NOTIFICATION = '#automation'

    public static final def PERMISSIONS = [
            'hudson.model.Item.Delete',
            'hudson.model.Item.Configure',
            'hudson.model.Item.Read',
            'hudson.model.Item.ExtendedRead',
            'hudson.model.Item.Discover',
            'hudson.model.Item.Build',
            'hudson.model.Item.Workspace',
            'hudson.model.Item.Cancel',
            'hudson.model.Run.Delete',
            'hudson.model.Run.Update',
            'hudson.scm.SCM.Tag']

    def enum API_ENVIRONMENTS {

        KAEV, DETA, TSAGE

        static List<String> listEnvs() {
            List<String> result = new LinkedList<>()
            values().each { env -> result.add(env.name())
            }
            return result
        }

        static List<String> listEnvGlobalContent() {
            List<String> result = new LinkedList<>()
            values().each { env ->
                if (env.toString() == "KAEV" || env.toString() == "TSAGE") {
                    result.add(env.name())
                }
            }
            return result
        }


        static List<String> listEnvNextGen() {
            List<String> result = new LinkedList<>()
            values().each {
                env ->
                    if (env.toString() == "TSAGE") {
                        result.add(env.name())
                    }
            }
            return result
        }
    }

    static Map<String, String> CEP_MAP = [
            "ADER": "Automation.xml"
    ]

    static String addEnvironmentGoal(def api_environment, def cepService) {
        return "-DbaseUri=${api_environment}" + "_" + "${cepService}"
    }

}