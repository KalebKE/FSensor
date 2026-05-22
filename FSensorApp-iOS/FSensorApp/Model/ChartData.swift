import Foundation

struct ChartSample: Identifiable {
    let id = UUID()
    let time: Float
    let x: Float
    let y: Float
    let z: Float
}

class ChartData: ObservableObject {
    private let capacity: Int
    private var samples: [ChartSample] = []

    init(capacity: Int = 250) {
        self.capacity = capacity
        samples.reserveCapacity(capacity)
    }

    var count: Int { samples.count }

    func append(x: Float, y: Float, z: Float, time: Float) {
        samples.append(ChartSample(time: time, x: x, y: y, z: z))
        if samples.count > capacity {
            samples.removeFirst()
        }
    }

    func allSamples() -> [ChartSample] {
        return samples
    }

    func clear() {
        samples.removeAll(keepingCapacity: true)
    }
}
