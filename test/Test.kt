import org.junit.Test

class MainKtTest {

    private lateinit var reportServer: ReportServer
    private lateinit var eventStorage: EventStorage
    private lateinit var managerAdminServer: ManagerAdminServer
    private lateinit var turnslite: Turnslite

    fun test(func: MainKtTest.() -> Unit) {
        reportServer = ReportServer()
        eventStorage = EventStorage(reportServer.getAddress())
        managerAdminServer = ManagerAdminServer(eventStorage.getAddress())
        turnslite = Turnslite(eventStorage.getAddress())

        func()

        reportServer.shutdown()
        eventStorage.shutdown()
    }

    @Test
    fun test0() = test {
        assert(!turnslite.attemptToEnter(1, 60))

        managerAdminServer.payFor(1, 100, 50)

        assert(turnslite.attemptToEnter(1, 60))
        assert(turnslite.check(1, 60))
        assert(managerAdminServer.check(1, 60))
        assert(!turnslite.check(1, 110))

        turnslite.attemptToLeave(1, 70)

        assert(reportServer.getReport(1, 70).frequency == 1.0)
        assert(reportServer.getReport(1, 80).frequency == 0.5)
    }

    @Test
    fun test1() = test {
        assert(!turnslite.attemptToEnter(1, 60))

        managerAdminServer.payFor(1, 100, 50)

        assert(!turnslite.attemptToEnter(2, 60))
        assert(!turnslite.check(2, 60))
        assert(!managerAdminServer.check(2, 60))
        assert(!turnslite.check(2, 110))

        assert(reportServer.getReport(1, 70).frequency == 0.0)
        assert(reportServer.getReport(1, 80).frequency == 0.0)
    }

    @Test
    fun test2() = test {
        managerAdminServer.payFor(1, 100, 50)

        turnslite.attemptToEnter(1, 60)
        turnslite.attemptToLeave(1, 70)
        turnslite.attemptToEnter(1, 80)
        turnslite.attemptToLeave(1, 90)

        assert(reportServer.getReport(1, 100).frequency == 0.5)
    }

    @Test
    fun test3() = test {
        managerAdminServer.payFor(1, 10, 0)
        managerAdminServer.payFor(1, 100, 1)
        managerAdminServer.payFor(1, 1000, 2)

        assert(turnslite.attemptToEnter(1, 3))
        turnslite.attemptToLeave(1, 4)

        assert(turnslite.attemptToEnter(1, 5))
        turnslite.attemptToLeave(1, 6)

        assert(reportServer.getReport(1, 7).firstVisit == 3L)
        assert(reportServer.getReport(1, 7).lastVisit == 5L)
        assert(reportServer.getReport(1, 7).visits == 2)
        assert(reportServer.getReport(1, 7).userId == 1L)
    }

    @Test
    fun test4() = test {
        managerAdminServer.payFor(1, 10, 0)
        managerAdminServer.payFor(2, 100, 1)
        managerAdminServer.payFor(2, 1000, 2)

        assert(!managerAdminServer.check(1, 50))
        assert(managerAdminServer.check(2, 50))
        assert(!managerAdminServer.check(3, 50))
    }
}