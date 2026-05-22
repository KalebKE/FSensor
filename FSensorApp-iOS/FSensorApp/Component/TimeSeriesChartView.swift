import SwiftUI
import Charts

struct ChartPoint: Identifiable {
    let id = UUID()
    let time: Float
    let value: Float
    let axis: String
}

struct TimeSeriesChartView: View {
    let data: ChartData
    let yRange: Float
    let unit: String

    var body: some View {
        let samples = data.allSamples()

        if samples.count < 2 {
            Text("Waiting for data...")
                .foregroundStyle(.secondary)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else {
            let latestTime = samples.last!.time
            let points = buildPoints(samples: samples, latestTime: latestTime)

            Chart(points) { point in
                LineMark(
                    x: .value("Time", point.time),
                    y: .value("Value", point.value)
                )
                .foregroundStyle(by: .value("Axis", point.axis))
            }
            .chartForegroundStyleScale([
                "X": Color.red,
                "Y": Color.green,
                "Z": Color.blue
            ])
            .chartXScale(domain: (latestTime - 5)...latestTime)
            .chartYScale(domain: -yRange...yRange)
            .chartXAxis {
                AxisMarks(values: .automatic(desiredCount: 5))
            }
            .chartYAxis {
                AxisMarks(values: .automatic(desiredCount: 5))
            }
            .chartLegend(.hidden)
        }
    }

    private func buildPoints(samples: [ChartSample], latestTime: Float) -> [ChartPoint] {
        let step = max(1, samples.count / 50)
        var points: [ChartPoint] = []
        points.reserveCapacity(150)
        for i in stride(from: 0, to: samples.count, by: step) {
            let s = samples[i]
            if s.time >= latestTime - 5 {
                points.append(ChartPoint(time: s.time, value: s.x, axis: "X"))
                points.append(ChartPoint(time: s.time, value: s.y, axis: "Y"))
                points.append(ChartPoint(time: s.time, value: s.z, axis: "Z"))
            }
        }
        return points
    }
}
