package xf.xfvrp.report

import spock.lang.Specification
import util.instances.TestNode
import util.instances.TestVehicle
import xf.xfvrp.base.*
import xf.xfvrp.base.metric.EucledianMetric
import xf.xfvrp.base.metric.internal.AcceleratedMetricTransformator
import xf.xfvrp.opt.Solution
import xf.xfvrp.opt.XFVRPSolution
import xf.xfvrp.report.build.ReportBuilder

class ReportBuilderCapacitySpec extends Specification {

	def service = new ReportBuilder();

	def nd = new TestNode(
	externID: "DEP",
	siteType: SiteType.DEPOT,
	demand: [0, 0],
	timeWindow: [[0,99],[2,99]]
	).getNode()

	def nr = new TestNode(
	externID: "REP",
	siteType: SiteType.REPLENISH,
	demand: [0, 0],
	timeWindow: [[0,99],[2,99]]
	).getNode()

	def sol;

	def parameter = new XFVRPParameter()

	def metric = new EucledianMetric()

	def "Distance"() {
		def v = new TestVehicle(name: "V1", capacity: [3, 3]).getVehicle()
		def model = initScen1(v, LoadType.DELIVERY)
		def n = model.getNodes()

		sol = new Solution()
		sol.setGiantRoute([nd, n[1], n[2], n[3], nd] as Node[])

		def solution = new XFVRPSolution(sol, model);

		when:
		def result = service.getReport(solution)

		then:
		result != null
		result.getSummary().getOverloads()[0] == 0
		result.getRoutes().size() == 1
		result.getRoutes().get(0).getSummary().getNbrOfEvents() == 5
		result.getRoutes().get(0).getEvents().size() == 5
		Math.abs(result.getRoutes().get(0).getEvents().get(0).getDistance() - 0) < 0.001
		Math.abs(result.getRoutes().get(0).getEvents().get(1).getDistance() - 1.414) < 0.001
		Math.abs(result.getRoutes().get(0).getEvents().get(2).getDistance() - 1) < 0.001
		Math.abs(result.getRoutes().get(0).getEvents().get(3).getDistance() - 1.414) < 0.001
		Math.abs(result.getRoutes().get(0).getEvents().get(4).getDistance() - 1) < 0.001
		Math.abs(result.getRoutes().get(0).getSummary().getDistance() - 4.828) < 0.001
	}

	def "Delivery - 2 capacity, all clear"() {
		def v = new TestVehicle(name: "V1", capacity: [3, 3]).getVehicle()
		def model = initScen1(v, LoadType.DELIVERY)
		def n = model.getNodes()

		sol = new Solution()
		sol.setGiantRoute([nd, n[1], n[2], n[3], nd] as Node[])

		def solution = new XFVRPSolution(sol, model);

		when:
		def result = service.getReport(solution)

		then:
		result != null
		result.getSummary().getOverloads()[0] == 0
		result.getRoutes().size() == 1
		result.getRoutes().get(0).getSummary().getDeliveries()[0] == 3
		result.getRoutes().get(0).getSummary().getDeliveries()[1] == 3
		result.getRoutes().get(0).getSummary().getPickups()[0] == 0
		result.getRoutes().get(0).getSummary().getPickups()[1] == 0
		Math.abs(result.getRoutes().get(0).getEvents().get(0).getAmounts()[0] - 3) < 0.001
		result.getRoutes().get(0).getEvents().get(0).getLoadType() == LoadType.PICKUP
		Math.abs(result.getRoutes().get(0).getEvents().get(1).getAmounts()[0] - 1) < 0.001
		result.getRoutes().get(0).getEvents().get(1).getLoadType() == LoadType.DELIVERY
		Math.abs(result.getRoutes().get(0).getEvents().get(2).getAmounts()[0] - 1) < 0.001
		result.getRoutes().get(0).getEvents().get(2).getLoadType() == LoadType.DELIVERY
		Math.abs(result.getRoutes().get(0).getEvents().get(3).getAmounts()[0] - 1) < 0.001
		result.getRoutes().get(0).getEvents().get(3).getLoadType() == LoadType.DELIVERY
		Math.abs(result.getRoutes().get(0).getEvents().get(4).getAmounts()[0] - 0) < 0.001
		result.getRoutes().get(0).getEvents().get(4).getLoadType() == LoadType.UNDEF
	}

	def "Delivery - 2 capacity, first fail"() {
		def v = new TestVehicle(name: "V1", capacity: [1, 3]).getVehicle()
		def model = initScen1(v, LoadType.DELIVERY)
		def n = model.getNodes()

		sol = new Solution()
		sol.setGiantRoute([nd, n[1], n[2], n[3], nd] as Node[])

		def solution = new XFVRPSolution(sol, model);

		when:
		def result = service.getReport(solution)

		then:
		result != null
		result.getSummary().getOverload(v) == 2
		result.getSummary().getOverloads()[0] == 2
		result.getSummary().getOverloads()[1] == 0
		result.getRoutes().size() == 1
		result.getRoutes().get(0).getSummary().getDeliveries()[0] == 3
		result.getRoutes().get(0).getSummary().getDeliveries()[1] == 3
		result.getRoutes().get(0).getSummary().getPickups()[0] == 0
		result.getRoutes().get(0).getSummary().getPickups()[1] == 0
		Math.abs(result.getRoutes().get(0).getEvents().get(0).getAmounts()[0] - 3) < 0.001
		result.getRoutes().get(0).getEvents().get(0).getLoadType() == LoadType.PICKUP
		Math.abs(result.getRoutes().get(0).getEvents().get(1).getAmounts()[0] - 1) < 0.001
		result.getRoutes().get(0).getEvents().get(1).getLoadType() == LoadType.DELIVERY
		Math.abs(result.getRoutes().get(0).getEvents().get(2).getAmounts()[0] - 1) < 0.001
		result.getRoutes().get(0).getEvents().get(2).getLoadType() == LoadType.DELIVERY
		Math.abs(result.getRoutes().get(0).getEvents().get(3).getAmounts()[0] - 1) < 0.001
		result.getRoutes().get(0).getEvents().get(3).getLoadType() == LoadType.DELIVERY
		Math.abs(result.getRoutes().get(0).getEvents().get(4).getAmounts()[0] - 0) < 0.001
		result.getRoutes().get(0).getEvents().get(4).getLoadType() == LoadType.UNDEF
	}

	def "Delivery - 2 capacity, second fail"() {
		def v = new TestVehicle(name: "V1", capacity: [3, 2]).getVehicle()
		def model = initScen1(v, LoadType.DELIVERY)
		def n = model.getNodes()

		sol = new Solution()
		sol.setGiantRoute([nd, n[1], n[2], n[3], nd] as Node[])

		def solution = new XFVRPSolution(sol, model);

		when:
		def result = service.getReport(solution)

		then:
		result != null
		result.getSummary().getOverload(v) == 0
		result.getSummary().getOverloads()[0] == 0
		result.getSummary().getOverloads()[1] == 1
		result.getRoutes().size() == 1
		result.getRoutes().get(0).getSummary().getDeliveries()[0] == 3
		result.getRoutes().get(0).getSummary().getDeliveries()[1] == 3
		result.getRoutes().get(0).getSummary().getPickups()[0] == 0
		result.getRoutes().get(0).getSummary().getPickups()[1] == 0
		Math.abs(result.getRoutes().get(0).getEvents().get(0).getAmounts()[0] - 3) < 0.001
		result.getRoutes().get(0).getEvents().get(0).getLoadType() == LoadType.PICKUP
		Math.abs(result.getRoutes().get(0).getEvents().get(1).getAmounts()[0] - 1) < 0.001
		result.getRoutes().get(0).getEvents().get(1).getLoadType() == LoadType.DELIVERY
		Math.abs(result.getRoutes().get(0).getEvents().get(2).getAmounts()[0] - 1) < 0.001
		result.getRoutes().get(0).getEvents().get(2).getLoadType() == LoadType.DELIVERY
		Math.abs(result.getRoutes().get(0).getEvents().get(3).getAmounts()[0] - 1) < 0.001
		result.getRoutes().get(0).getEvents().get(3).getLoadType() == LoadType.DELIVERY
		Math.abs(result.getRoutes().get(0).getEvents().get(4).getAmounts()[0] - 0) < 0.001
		result.getRoutes().get(0).getEvents().get(4).getLoadType() == LoadType.UNDEF

	}

	def "Pickup - 2 capacity, all clear"() {
		def v = new TestVehicle(name: "V1", capacity: [3, 3]).getVehicle()
		def model = initScen1(v, LoadType.PICKUP)
		def n = model.getNodes()

		sol = new Solution()
		sol.setGiantRoute([nd, n[1], n[2], n[3], nd] as Node[])

		def solution = new XFVRPSolution(sol, model);

		when:
		def result = service.getReport(solution)

		then:
		result != null
		result.getSummary().getOverloads()[0] == 0
		result.getSummary().getOverloads()[1] == 0
		result.getRoutes().size() == 1
		result.getRoutes().get(0).getSummary().getDeliveries()[0] == 0
		result.getRoutes().get(0).getSummary().getDeliveries()[1] == 0
		result.getRoutes().get(0).getSummary().getPickups()[0] == 3
		result.getRoutes().get(0).getSummary().getPickups()[1] == 3
		Math.abs(result.getRoutes().get(0).getEvents().get(0).getAmounts()[0] - 0) < 0.001
		result.getRoutes().get(0).getEvents().get(0).getLoadType() == LoadType.PICKUP
		Math.abs(result.getRoutes().get(0).getEvents().get(1).getAmounts()[0] - 1) < 0.001
		result.getRoutes().get(0).getEvents().get(1).getLoadType() == LoadType.PICKUP
		Math.abs(result.getRoutes().get(0).getEvents().get(2).getAmounts()[0] - 1) < 0.001
		result.getRoutes().get(0).getEvents().get(2).getLoadType() == LoadType.PICKUP
		Math.abs(result.getRoutes().get(0).getEvents().get(3).getAmounts()[0] - 1) < 0.001
		result.getRoutes().get(0).getEvents().get(3).getLoadType() == LoadType.PICKUP
		Math.abs(result.getRoutes().get(0).getEvents().get(4).getAmounts()[0] - 0) < 0.001
		result.getRoutes().get(0).getEvents().get(4).getLoadType() == LoadType.UNDEF
	}

	def "Pickup - 2 capacity, first fail"() {
		def v = new TestVehicle(name: "V1", capacity: [1, 3]).getVehicle()
		def model = initScen1(v, LoadType.PICKUP)
		def n = model.getNodes()

		sol = new Solution()
		sol.setGiantRoute([nd, n[1], n[2], n[3], nd] as Node[])

		def solution = new XFVRPSolution(sol, model)

		when:
		def result = service.getReport(solution)

		then:
		result != null
		result.getSummary().getOverloads()[0] == 2
		result.getSummary().getOverloads()[1] == 0
		result.getRoutes().size() == 1
		result.getRoutes().get(0).getSummary().getDeliveries()[0] == 0
		result.getRoutes().get(0).getSummary().getDeliveries()[1] == 0
		result.getRoutes().get(0).getSummary().getPickups()[0] == 3
		result.getRoutes().get(0).getSummary().getPickups()[1] == 3
		Math.abs(result.getRoutes().get(0).getEvents().get(0).getAmounts()[0] - 0) < 0.001
		result.getRoutes().get(0).getEvents().get(0).getLoadType() == LoadType.PICKUP
		Math.abs(result.getRoutes().get(0).getEvents().get(1).getAmounts()[0] - 1) < 0.001
		result.getRoutes().get(0).getEvents().get(1).getLoadType() == LoadType.PICKUP
		Math.abs(result.getRoutes().get(0).getEvents().get(2).getAmounts()[0] - 1) < 0.001
		result.getRoutes().get(0).getEvents().get(2).getLoadType() == LoadType.PICKUP
		Math.abs(result.getRoutes().get(0).getEvents().get(3).getAmounts()[0] - 1) < 0.001
		result.getRoutes().get(0).getEvents().get(3).getLoadType() == LoadType.PICKUP
		Math.abs(result.getRoutes().get(0).getEvents().get(4).getAmounts()[0] - 0) < 0.001
		result.getRoutes().get(0).getEvents().get(4).getLoadType() == LoadType.UNDEF
	}

	def "Pickup - 2 capacity, second fail"() {
		def v = new TestVehicle(name: "V1", capacity: [3, 2]).getVehicle()
		def model = initScen1(v, LoadType.PICKUP)
		def n = model.getNodes()

		sol = new Solution()
		sol.setGiantRoute([nd, n[1], n[2], n[3], nd] as Node[])

		def solution = new XFVRPSolution(sol, model);

		when:
		def result = service.getReport(solution)

		then:
		result != null
		result.getSummary().getOverloads()[0] == 0
		result.getSummary().getOverloads()[1] == 1
		result.getRoutes().size() == 1
		result.getRoutes().get(0).getSummary().getDeliveries()[0] == 0
		result.getRoutes().get(0).getSummary().getDeliveries()[1] == 0
		result.getRoutes().get(0).getSummary().getPickups()[0] == 3
		result.getRoutes().get(0).getSummary().getPickups()[1] == 3
		Math.abs(result.getRoutes().get(0).getEvents().get(0).getAmounts()[0] - 0) < 0.001
		result.getRoutes().get(0).getEvents().get(0).getLoadType() == LoadType.PICKUP
		Math.abs(result.getRoutes().get(0).getEvents().get(1).getAmounts()[0] - 1) < 0.001
		result.getRoutes().get(0).getEvents().get(1).getLoadType() == LoadType.PICKUP
		Math.abs(result.getRoutes().get(0).getEvents().get(2).getAmounts()[0] - 1) < 0.001
		result.getRoutes().get(0).getEvents().get(2).getLoadType() == LoadType.PICKUP
		Math.abs(result.getRoutes().get(0).getEvents().get(3).getAmounts()[0] - 1) < 0.001
		result.getRoutes().get(0).getEvents().get(3).getLoadType() == LoadType.PICKUP
		Math.abs(result.getRoutes().get(0).getEvents().get(4).getAmounts()[0] - 0) < 0.001
		result.getRoutes().get(0).getEvents().get(4).getLoadType() == LoadType.UNDEF
	}

	def "Pickup/Delivery - all clear"() {
		def v = new TestVehicle(name: "V1", capacity: [3, 3]).getVehicle()
		def model = initScen2(v)
		def n = model.getNodes()

		sol = new Solution()
		sol.setGiantRoute([nd, n[1], n[3], n[2], nd] as Node[])

		def solution = new XFVRPSolution(sol, model);

		when:
		def result = service.getReport(solution)

		then:
		result != null
		result.getSummary().getOverloads()[0] == 0
		result.getSummary().getOverloads()[1] == 0
		result.getRoutes().size() == 1
		result.getRoutes().get(0).getSummary().getDeliveries()[0] == 3
		result.getRoutes().get(0).getSummary().getDeliveries()[1] == 2
		result.getRoutes().get(0).getSummary().getPickups()[0] == 3
		result.getRoutes().get(0).getSummary().getPickups()[1] == 1
		Math.abs(result.getRoutes().get(0).getEvents().get(0).getAmounts()[0] - 3) < 0.001
		result.getRoutes().get(0).getEvents().get(0).getLoadType() == LoadType.PICKUP
		Math.abs(result.getRoutes().get(0).getEvents().get(1).getAmounts()[0] - 1) < 0.001
		result.getRoutes().get(0).getEvents().get(1).getLoadType() == LoadType.DELIVERY
		Math.abs(result.getRoutes().get(0).getEvents().get(2).getAmounts()[0] - 2) < 0.001
		result.getRoutes().get(0).getEvents().get(2).getLoadType() == LoadType.DELIVERY
		Math.abs(result.getRoutes().get(0).getEvents().get(3).getAmounts()[0] - 3) < 0.001
		result.getRoutes().get(0).getEvents().get(3).getLoadType() == LoadType.PICKUP
		Math.abs(result.getRoutes().get(0).getEvents().get(4).getAmounts()[0] - 0) < 0.001
		result.getRoutes().get(0).getEvents().get(4).getLoadType() == LoadType.UNDEF

		Math.abs(result.getRoutes().get(0).getEvents().get(0).getAmounts()[1] - 2) < 0.001
		result.getRoutes().get(0).getEvents().get(0).getLoadType() == LoadType.PICKUP
		Math.abs(result.getRoutes().get(0).getEvents().get(1).getAmounts()[1] - 1) < 0.001
		result.getRoutes().get(0).getEvents().get(1).getLoadType() == LoadType.DELIVERY
		Math.abs(result.getRoutes().get(0).getEvents().get(2).getAmounts()[1] - 1) < 0.001
		result.getRoutes().get(0).getEvents().get(2).getLoadType() == LoadType.DELIVERY
		Math.abs(result.getRoutes().get(0).getEvents().get(3).getAmounts()[1] - 1) < 0.001
		result.getRoutes().get(0).getEvents().get(3).getLoadType() == LoadType.PICKUP
		Math.abs(result.getRoutes().get(0).getEvents().get(4).getAmounts()[1] - 0) < 0.001
		result.getRoutes().get(0).getEvents().get(4).getLoadType() == LoadType.UNDEF
	}

	def "Pickup/Delivery - wrong route order"() {
		def v = new TestVehicle(name: "V1", capacity: [3, 3]).getVehicle()
		def model = initScen2(v)
		def n = model.getNodes()

		sol = new Solution()
		sol.setGiantRoute([nd, n[1], n[2], n[3], nd] as Node[])

		def solution = new XFVRPSolution(sol, model);

		when:
		def result = service.getReport(solution)

		then:
		result != null

		result.getSummary().getOverloads()[0] == 3
		result.getSummary().getOverloads()[1] == 0
		result.getRoutes().size() == 1
		result.getRoutes().get(0).getSummary().getDeliveries()[0] == 3
		result.getRoutes().get(0).getSummary().getDeliveries()[1] == 2
		result.getRoutes().get(0).getSummary().getPickups()[0] == 3
		result.getRoutes().get(0).getSummary().getPickups()[1] == 1

		Math.abs(result.getRoutes().get(0).getEvents().get(0).getAmounts()[0] - 3) < 0.001
		result.getRoutes().get(0).getEvents().get(0).getLoadType() == LoadType.PICKUP
		Math.abs(result.getRoutes().get(0).getEvents().get(1).getAmounts()[0] - 1) < 0.001
		result.getRoutes().get(0).getEvents().get(1).getLoadType() == LoadType.DELIVERY
		Math.abs(result.getRoutes().get(0).getEvents().get(2).getAmounts()[0] - 3) < 0.001
		result.getRoutes().get(0).getEvents().get(2).getLoadType() == LoadType.PICKUP
		Math.abs(result.getRoutes().get(0).getEvents().get(3).getAmounts()[0] - 2) < 0.001
		result.getRoutes().get(0).getEvents().get(3).getLoadType() == LoadType.DELIVERY
		Math.abs(result.getRoutes().get(0).getEvents().get(4).getAmounts()[0] - 0) < 0.001
		result.getRoutes().get(0).getEvents().get(4).getLoadType() == LoadType.UNDEF
	}

	def "Replenish - homogeneous - all clear"() {
		def v = new TestVehicle(name: "V1", capacity: [3, 3]).getVehicle()
		def model = initScen3(v)
		def n = model.getNodes()

		sol = new Solution()
		sol.setGiantRoute([nd, n[2], n[4], nr, n[6], n[3], nd] as Node[])

		def solution = new XFVRPSolution(sol, model);

		when:
		def result = service.getReport(solution)

		then:
		result != null
		result.getSummary().getOverloads()[0] == 0
		result.getSummary().getOverloads()[1] == 0
		result.getRoutes().size() == 1
		result.getRoutes().get(0).getSummary().getDeliveries()[0] == 4
		result.getRoutes().get(0).getSummary().getDeliveries()[1] == 3
		result.getRoutes().get(0).getSummary().getPickups()[0] == 3
		result.getRoutes().get(0).getSummary().getPickups()[1] == 1

		checkAmount(result, 0, 3, LoadType.PICKUP)
		checkAmount(result, 1, 1, LoadType.DELIVERY)
		checkAmount(result, 2, 2, LoadType.DELIVERY)
		checkAmount(result, 3, 1, LoadType.PICKUP)
		checkAmount(result, 4, 1, LoadType.DELIVERY)
		checkAmount(result, 5, 3, LoadType.PICKUP)
		checkAmount(result, 6, 0, LoadType.UNDEF)
	}

	def "Replenish - homogeneous - pickup fail"() {
		def v = new TestVehicle(name: "V1", capacity: [3, 3]).getVehicle()
		def model = initScen3(v)
		def n = model.getNodes()

		sol = new Solution()
		sol.setGiantRoute([nd, n[2], n[4], nr, n[3], n[5], nd] as Node[])

		def solution = new XFVRPSolution(sol, model);

		when:
		def result = service.getReport(solution)

		then:
		result != null

		result.getSummary().getOverloads()[0] == 2
		result.getSummary().getOverloads()[1] == 0
		result.getRoutes().size() == 1
		result.getRoutes().get(0).getSummary().getDeliveries()[0] == 3
		result.getRoutes().get(0).getSummary().getDeliveries()[1] == 2
		result.getRoutes().get(0).getSummary().getPickups()[0] == 5
		result.getRoutes().get(0).getSummary().getPickups()[1] == 2

		checkAmount(result, 0, 3, LoadType.PICKUP)
		checkAmount(result, 1, 1, LoadType.DELIVERY)
		checkAmount(result, 2, 2, LoadType.DELIVERY)
		checkAmount(result, 3, 0, LoadType.PICKUP)
		checkAmount(result, 4, 3, LoadType.PICKUP)
		checkAmount(result, 5, 2, LoadType.PICKUP)
		checkAmount(result, 6, 0, LoadType.UNDEF)
	}

	def "Replenish - homogeneous - delivery fail"() {
		def v = new TestVehicle(name: "V1", capacity: [3, 3]).getVehicle()
		def model = initScen3(v)
		def n = model.getNodes()

		sol = new Solution()
		sol.setGiantRoute([nd, n[2], n[4], n[6], nr, n[3], nd] as Node[])

		def solution = new XFVRPSolution(sol, model);

		when:
		def result = service.getReport(solution)

		then:
		result != null

		result.getSummary().getOverloads()[0] == 1
		result.getSummary().getOverloads()[1] == 0
		result.getRoutes().size() == 1
		result.getRoutes().get(0).getSummary().getDeliveries()[0] == 4
		result.getRoutes().get(0).getSummary().getDeliveries()[1] == 3
		result.getRoutes().get(0).getSummary().getPickups()[0] == 3
		result.getRoutes().get(0).getSummary().getPickups()[1] == 1

		checkAmount(result, 0, 4, LoadType.PICKUP)
		checkAmount(result, 1, 1, LoadType.DELIVERY)
		checkAmount(result, 2, 2, LoadType.DELIVERY)
		checkAmount(result, 3, 1, LoadType.DELIVERY)
		checkAmount(result, 4, 0, LoadType.PICKUP)
		checkAmount(result, 5, 3, LoadType.PICKUP)
		checkAmount(result, 6, 0, LoadType.UNDEF)
	}

	def "Replenish - hetero - all clear"() {
		def v = new TestVehicle(name: "V1", capacity: [3, 3]).getVehicle()
		def model = initScen3(v)
		def n = model.getNodes()

		sol = new Solution()
		sol.setGiantRoute([nd, n[4], n[5], n[2], nr, n[6], n[3], nd] as Node[])

		def solution = new XFVRPSolution(sol, model);

		when:
		def result = service.getReport(solution)

		then:
		result != null
		result.getSummary().getOverloads()[0] == 0
		result.getSummary().getOverloads()[1] == 0
		result.getRoutes().size() == 1
		result.getRoutes().get(0).getSummary().getDeliveries()[0] == 4
		result.getRoutes().get(0).getSummary().getDeliveries()[1] == 3
		result.getRoutes().get(0).getSummary().getPickups()[0] == 5
		result.getRoutes().get(0).getSummary().getPickups()[1] == 2

		checkAmount(result, 0, 3, LoadType.PICKUP)
		checkAmount(result, 1, 2, LoadType.DELIVERY)
		checkAmount(result, 2, 2, LoadType.PICKUP)
		checkAmount(result, 3, 1, LoadType.DELIVERY)
		checkAmount(result, 4, 1, LoadType.PICKUP)
		checkAmount(result, 5, 1, LoadType.DELIVERY)
		checkAmount(result, 6, 3, LoadType.PICKUP)
		checkAmount(result, 7, 0, LoadType.UNDEF)
	}

	private static void checkAmount(Report result, int eventIdx, float amountVal, LoadType type) {
		assert Math.abs(result.getRoutes().get(0).getEvents().get(eventIdx).getAmounts()[0] - amountVal) < 0.001
		assert result.getRoutes().get(0).getEvents().get(eventIdx).getLoadType() == type
	}


	XFVRPModel initScen1(Vehicle v, LoadType loadType) {
		def n1 = new TestNode(
				globalIdx: 1,
				externID: "1",
				xlong: 1,
				ylat: 1,
				geoId: 1,
				demand: [1, 1],
				timeWindow: [[0,2],[2,6]],
				loadType: loadType)
				.getNode()
		def n2 = new TestNode(
				globalIdx: 2,
				externID: "2",
				xlong: 0,
				ylat: 1,
				geoId: 2,
				demand: [1, 1],
				timeWindow: [[0,2],[2,6]],
				loadType: loadType)
				.getNode()
		def n3 = new TestNode(
				globalIdx: 3,
				externID: "3",
				xlong: 1,
				ylat: 0,
				geoId: 3,
				demand: [1, 1],
				timeWindow: [[0,2],[2,6]],
				loadType: loadType)
				.getNode()

		nd.setIdx(0);
		n1.setIdx(1);
		n2.setIdx(2);
		n3.setIdx(3);

		def nodes = [nd, n1, n2, n3] as Node[];

		def iMetric = new AcceleratedMetricTransformator().transform(metric, nodes, v);

		return new XFVRPModel(nodes, iMetric, iMetric, v, parameter)
	}

	XFVRPModel initScen2(Vehicle v) {
		def n1 = new TestNode(
				globalIdx: 1,
				externID: "1",
				xlong: 1,
				ylat: 1,
				geoId: 1,
				demand: [1, 1],
				timeWindow: [[0,2],[2,6]],
				loadType: LoadType.DELIVERY)
				.getNode()
		def n2 = new TestNode(
				globalIdx: 2,
				externID: "2",
				xlong: 0,
				ylat: 1,
				geoId: 2,
				demand: [3, 1],
				timeWindow: [[0,2],[2,6]],
				loadType: LoadType.PICKUP)
				.getNode()
		def n3 = new TestNode(
				globalIdx: 3,
				externID: "3",
				xlong: 1,
				ylat: 0,
				geoId: 3,
				demand: [2, 1],
				timeWindow: [[0,2],[2,6]],
				loadType: LoadType.DELIVERY)
				.getNode()

		nd.setIdx(0);
		n1.setIdx(1);
		n2.setIdx(2);
		n3.setIdx(3);

		def nodes = [nd, n1, n2, n3] as Node[];

		def iMetric = new AcceleratedMetricTransformator().transform(metric, nodes, v);

		return new XFVRPModel(nodes, iMetric, iMetric, v, parameter)
	}

	XFVRPModel initScen3(Vehicle v) {
		def n1 = new TestNode(
				globalIdx: 1,
				externID: "1",
				xlong: 1,
				ylat: 1,
				geoId: 1,
				demand: [1, 1],
				timeWindow: [[0,9],[2,9]],
				loadType: LoadType.DELIVERY)
				.getNode()
		def n2 = new TestNode(
				globalIdx: 2,
				externID: "2",
				xlong: 0,
				ylat: 1,
				geoId: 2,
				demand: [3, 1],
				timeWindow: [[0,9],[2,9]],
				loadType: LoadType.PICKUP)
				.getNode()
		def n3 = new TestNode(
				globalIdx: 3,
				externID: "3",
				xlong: 1,
				ylat: 0,
				geoId: 3,
				demand: [2, 1],
				timeWindow: [[0,9],[2,9]],
				loadType: LoadType.DELIVERY)
				.getNode()
		def n4 = new TestNode(
				globalIdx: 4,
				externID: "4",
				xlong: 0,
				ylat: -1,
				geoId: 4,
				demand: [2, 1],
				timeWindow: [[0,9],[2,9]],
				loadType: LoadType.PICKUP)
				.getNode()
		def n5 = new TestNode(
				globalIdx: 5,
				externID: "5",
				xlong: -1,
				ylat: 0,
				geoId: 5,
				demand: [1, 1],
				timeWindow: [[0,9],[2,9]],
				loadType: LoadType.DELIVERY)
				.getNode()

		nd.setIdx(0);
		nr.setIdx(1);
		n1.setIdx(2);
		n2.setIdx(3);
		n3.setIdx(4);
		n4.setIdx(5);
		n5.setIdx(6);

		def nodes = [nd, nr, n1, n2, n3, n4, n5] as Node[];

		def iMetric = new AcceleratedMetricTransformator().transform(metric, nodes, v);

		return new XFVRPModel(nodes, iMetric, iMetric, v, parameter)
	}

}
