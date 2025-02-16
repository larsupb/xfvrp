package xf.xfvrp.opt.improve

import spock.lang.Specification
import util.instances.TestNode
import util.instances.TestVehicle
import xf.xfvrp.base.*
import xf.xfvrp.base.fleximport.CustomerData
import xf.xfvrp.base.metric.EucledianMetric
import xf.xfvrp.base.metric.internal.AcceleratedMetricTransformator
import xf.xfvrp.opt.Solution

class XFPDPRelocateExtSpec extends Specification {

	def service = new XFPDPRelocate();

	def nd = new TestNode(
	externID: "DEP",
	globalIdx: 0,
	siteType: SiteType.DEPOT,
	demand: [0, 0],
	timeWindow: [[0,99],[2,99]]
	).getNode()

	def sol;

	def parameter = new XFVRPParameter()

	def metric = new EucledianMetric()

	def "Search"() {
		def model = initScen()
		def n = model.getNodes()
		service.setModel(model)

		sol = new Solution()
		sol.setGiantRoute([nd, n[1], n[2], nd, n[3], n[4], nd] as Node[])

		when:
		def impList = service.search(sol.getGiantRoute())

		then:
		impList.stream().filter({f -> f[0] == 1 && f[1] == 2 && f[2] == 4 && f[3] == 4}).count() == 1
		impList.stream().filter({f -> f[0] == 1 && f[1] == 2 && f[2] == 6 && f[3] == 6}).count() == 1
		impList.stream().filter({f -> f[0] == 1 && f[1] == 2 && f[2] == 4 && f[3] == 5}).count() == 1
		impList.stream().filter({f -> f[0] == 1 && f[1] == 2 && f[2] == 4 && f[3] == 6}).count() == 1
		impList.stream().filter({f -> f[0] == 4 && f[1] == 5 && f[2] == 1 && f[3] == 1}).count() == 1
		impList.stream().filter({f -> f[0] == 4 && f[1] == 5 && f[2] == 2 && f[3] == 2}).count() == 1
		impList.stream().filter({f -> f[0] == 4 && f[1] == 5 && f[2] == 3 && f[3] == 3}).count() == 1
		impList.stream().filter({f -> f[0] == 4 && f[1] == 5 && f[2] == 2 && f[3] == 3}).count() == 1
	}
	
	def "Potential 1"() {
		def model = initScen()
		def n = model.getNodes()
		service.setModel(model)

		sol = new Solution()
		sol.setGiantRoute([nd, n[3], n[4], n[1], n[2], nd] as Node[])
		def route = sol.getGiantRoute()

		when:
		def result = service.getPotential(route, 3, 4, 1, 1)
		
		then:
		result == -2
	}
	
	def "Potential 2"() {
		def model = initScen()
		def n = model.getNodes()
		service.setModel(model)

		sol = new Solution()
		sol.setGiantRoute([nd, n[3], n[1], n[4], n[2], nd] as Node[])
		def route = sol.getGiantRoute()

		when:
		def result = service.getPotential(route, 2, 4, 1, 3)
		
		then:
		result == -2
	}
	
	def "Potential 3"() {
		def model = initScen()
		def n = model.getNodes()
		service.setModel(model)

		sol = new Solution()
		sol.setGiantRoute([nd, n[3], n[1], n[4], n[2], nd] as Node[])
		def route = sol.getGiantRoute()

		when:
		def result = service.getPotential(route, 1, 3, 4, 5)
		
		then:
		result == -2
	}
	
	def "Potential 4"() {
		def model = initScen()
		def n = model.getNodes()
		service.setModel(model)

		sol = new Solution()
		sol.setGiantRoute([nd, n[1], n[2], n[3], n[4], nd] as Node[])
		def route = sol.getGiantRoute()

		when:
		def result = service.getPotential(route, 3, 4, 1, 2)
		
		then:
		result == 4
	}
	
	def "Potential no move"() {
		def model = initScen()
		def n = model.getNodes()
		service.setModel(model)

		sol = new Solution()
		sol.setGiantRoute([nd, n[1], n[2], n[3], n[4], nd] as Node[])
		def route = sol.getGiantRoute()

		when:
		def result = service.getPotential(route, 1, 2, 3, 3)
		
		then:
		result == 0
	}
	
	def "Potential partial move"() {
		def model = initScen()
		def n = model.getNodes()
		service.setModel(model)

		sol = new Solution()
		sol.setGiantRoute([nd, n[2], n[3], n[4], n[1], nd] as Node[])
		def route = sol.getGiantRoute()

		when:
		def result = service.getPotential(route, 4, 1, 2, 2)
		
		then:
		result == 0
	}
	
	XFVRPModel initScen() {
		def v = new TestVehicle(name: "V1", capacity: [3, 3]).getVehicle()

		def n1 = new TestNode(
				globalIdx: 1,
				externID: "1",
				xlong: 1,
				ylat: 0,
				geoId: 1,
				demand: [2, 2],
				timeWindow: [[0,99]],
				shipID: "A",
				loadType: LoadType.PICKUP)
				.getNode()
		def n2 = new TestNode(
				globalIdx: 2,
				externID: "2",
				xlong: 2,
				ylat: 0,
				geoId: 2,
				demand: [-2, -2],
				timeWindow: [[0,99]],
				shipID: "A",
				loadType: LoadType.DELIVERY)
				.getNode()
		def n3 = new TestNode(
				globalIdx: 3,
				externID: "3",
				xlong: 3,
				ylat: 0,
				geoId: 3,
				demand: [1, 1],
				timeWindow: [[0,99]],
				shipID: "B",
				loadType: LoadType.PICKUP)
				.getNode()
		def n4 = new TestNode(
				globalIdx: 4,
				externID: "4",
				xlong: 4,
				ylat: 0,
				geoId: 1,
				demand: [-1, -1],
				timeWindow: [[0,99]],
				shipID: "B",
				loadType: LoadType.DELIVERY)
				.getNode()
		def n5 = new TestNode(
				globalIdx: 5,
				externID: "5",
				xlong: 5,
				ylat: 0,
				geoId: 2,
				demand: [3, 3],
				timeWindow: [[0,99]],
				shipID: "C",
				loadType: LoadType.PICKUP)
				.getNode()
		def n6 = new TestNode(
				globalIdx: 6,
				externID: "6",
				xlong: 6,
				ylat: 0,
				geoId: 3,
				demand: [-3, -3],
				timeWindow: [[0,99]],
				shipID: "C",
				loadType: LoadType.DELIVERY)
				.getNode()

		def customers =	[
				new CustomerData(externID: "1", shipID: "A"),
				new CustomerData(externID: "2", shipID: "A"),
				new CustomerData(externID: "3", shipID: "B"),
				new CustomerData(externID: "4", shipID: "B"),
				new CustomerData(externID: "5", shipID: "C"),
				new CustomerData(externID: "6", shipID: "C")
		] as List<CustomerData>

		nd.setIdx(0);
		n1.setIdx(1);
		n2.setIdx(2);
		n3.setIdx(3);
		n4.setIdx(4);
		n5.setIdx(5);
		n6.setIdx(6);

		def nodes = [nd, n1, n2, n3, n4, n5, n6] as Node[];

		def iMetric = new AcceleratedMetricTransformator().transform(metric, nodes, v);
		new ShipmentConverter().convert(nodes, customers)

		return new XFVRPModel(nodes, iMetric, iMetric, v, parameter)
	}

}
