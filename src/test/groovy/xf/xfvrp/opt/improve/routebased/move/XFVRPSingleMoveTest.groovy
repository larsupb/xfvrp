package xf.xfvrp.opt.improve.routebased.move

import spock.lang.Specification
import util.instances.TestNode
import util.instances.TestVehicle
import xf.xfvrp.base.*
import xf.xfvrp.base.metric.EucledianMetric
import xf.xfvrp.base.metric.internal.AcceleratedMetricTransformator
import xf.xfvrp.opt.Solution

class XFVRPSingleMoveTest extends Specification {

    def service = new XFVRPSingleMove()

    def n1 = new Node(externID: "1", siteType: SiteType.DEPOT)
    def n2 = new Node(externID: "2", siteType: SiteType.CUSTOMER)
    def n3 = new Node(externID: "3", siteType: SiteType.CUSTOMER)
    def n4 = new Node(externID: "4", siteType: SiteType.DEPOT)
    def n5 = new Node(externID: "5", siteType: SiteType.CUSTOMER)
    def n6 = new Node(externID: "6", siteType: SiteType.CUSTOMER)
    def n7 = new Node(externID: "7", siteType: SiteType.DEPOT)

    def "change - reset - different routes"() {
        def sol = new Solution()
        sol.setGiantRoute([n1, n2, n3, n4, n5, n6, n7] as Node[])
        def parameter = [-1, 0, 1, 1, 2, 0, 0] as float[]

        when:
        XFVRPMoveUtil.change(sol, parameter)
        XFVRPMoveUtil.reverseChange(sol, parameter)
        def result = sol.getGiantRoute()
        then:
        result[0].externID == "1"
        result[1].externID == "2"
        result[2].externID == "3"
        result[3].externID == "4"
        result[4].externID == "5"
        result[5].externID == "6"
        result[6].externID == "4"
    }

    def "change - reset - same routes - src > dst"() {
        def sol = new Solution()
        sol.setGiantRoute([n1, n2, n3, n5, n6, n7] as Node[])
        def parameter = [-1, 0, 0, 1, 3, 0, 0] as float[]

        when:
        XFVRPMoveUtil.change(sol, parameter)
        XFVRPMoveUtil.reverseChange(sol, parameter)
        def result = sol.getGiantRoute()
        then:
        result[0].externID == "1"
        result[1].externID == "2"
        result[2].externID == "3"
        result[3].externID == "5"
        result[4].externID == "6"
        result[5].externID == "1"
    }

    def "change - reset - same routes - src < dst"() {
        def sol = new Solution()
        sol.setGiantRoute([n1, n2, n3, n5, n6, n7] as Node[])
        def parameter = [-1, 0, 0, 4, 1, 0, 0] as float[]

        when:
        XFVRPMoveUtil.change(sol, parameter)
        XFVRPMoveUtil.reverseChange(sol, parameter)
        def result = sol.getGiantRoute()
        then:
        result[0].externID == "1"
        result[1].externID == "2"
        result[2].externID == "3"
        result[3].externID == "5"
        result[4].externID == "6"
        result[5].externID == "1"
    }

    def "find an improvement"() {
        def model = initScen()
        def n = model.getNodes()

        def sol = new Solution()
        sol.setGiantRoute([n[0], n[3], n[1], n[2], n[4], n[0]] as Node[])

        when:
        def newQuality = service.improve(sol, new Quality(cost: Float.MAX_VALUE), model)
        def result = sol.getGiantRoute()
        then:
        newQuality.cost < 7
        result[0].externID == "1"
        result[1].externID == "2"
        result[2].externID == "3"
        result[3].externID == "5"
        result[4].externID == "6"
        result[5].externID == "1"
    }

    def "find no improvement anymore"() {
        def model = initScen()
        def n = model.getNodes()

        def sol = new Solution()
        sol.setGiantRoute([n[0], n[1], n[2], n[3], n[4], n[0]] as Node[])

        when:
        def newQuality = service.improve(sol, new Quality(cost: 6.236067), model)
        def result = sol.getGiantRoute()
        then:
        newQuality == null
        result[0].externID == "1"
        result[1].externID == "2"
        result[2].externID == "3"
        result[3].externID == "5"
        result[4].externID == "6"
        result[5].externID == "1"
    }

    XFVRPModel initScen() {
        def v = new TestVehicle(name: "V1", capacity: [5, 5]).getVehicle()

        n1 = new TestNode(
                globalIdx: 1,
                externID: "1",
                geoId: 1,
                siteType: SiteType.DEPOT,
                xlong: 0,
                ylat: 0,
                demand: [0, 0],
                timeWindow: [[0,99]],
                loadType: LoadType.DELIVERY)
                .getNode()
        n2 = new TestNode(
                globalIdx: 2,
                externID: "2",
                geoId: 2,
                xlong: -1,
                ylat: 0.5,
                demand: [1, 1],
                timeWindow: [[0,99]],
                loadType: LoadType.DELIVERY)
                .getNode()
        n3 = new TestNode(
                globalIdx: 3,
                externID: "3",
                geoId: 3,
                xlong: -1,
                ylat: 1.5f,
                demand: [1, 1],
                timeWindow: [[0,99]],
                loadType: LoadType.DELIVERY)
                .getNode()
        n4 = new TestNode(
                globalIdx: 4,
                externID: "4",
                siteType: SiteType.DEPOT,
                geoId: 4,
                xlong: 10,
                ylat: 10,
                demand: [0, 0],
                timeWindow: [[0,99]],
                loadType: LoadType.DELIVERY)
                .getNode()
        n5 = new TestNode(
                globalIdx: 5,
                externID: "5",
                geoId: 5,
                xlong: 1,
                ylat: 1.5,
                demand: [1, 1],
                timeWindow: [[0,99]],
                loadType: LoadType.DELIVERY)
                .getNode()
        n6 = new TestNode(
                globalIdx: 6,
                externID: "6",
                geoId: 6,
                xlong: 1,
                ylat: 0.5,
                demand: [1, 1],
                timeWindow: [[0,99]],
                loadType: LoadType.DELIVERY)
                .getNode()
        n7 = new TestNode(
                globalIdx: 7,
                externID: "7",
                siteType: SiteType.DEPOT,
                geoId: 7,
                xlong: 1,
                ylat: 1,
                demand: [0, 0],
                timeWindow: [[0,99]],
                loadType: LoadType.DELIVERY)
                .getNode()

        n1.setIdx(0)
        n2.setIdx(1)
        n3.setIdx(2)
        n5.setIdx(3)
        n6.setIdx(4)

        def nodes = [n1, n2, n3, n5, n6] as Node[]

        def iMetric = new AcceleratedMetricTransformator().transform(new EucledianMetric(), nodes, v)

        return new XFVRPModel(nodes, iMetric, iMetric, v, new XFVRPParameter())
    }
}
