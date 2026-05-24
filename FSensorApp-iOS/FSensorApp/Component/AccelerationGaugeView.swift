import SwiftUI

struct AccelerationGaugeView: View {
    let x: Float
    let y: Float

    private let gravity: Float = 9.81

    var body: some View {
        Canvas { context, size in
            let s = min(size.width, size.height)
            let cx = size.width / 2
            let cy = size.height / 2
            let outerR = s * 0.44

            // Glow rings
            let glow1 = Path(ellipseIn: CGRect(x: cx - outerR * 1.06, y: cy - outerR * 1.06, width: outerR * 2.12, height: outerR * 2.12))
            context.stroke(glow1, with: .color(.neonCyan.opacity(0.08)), lineWidth: s * 0.04)

            let glow2 = Path(ellipseIn: CGRect(x: cx - outerR * 1.03, y: cy - outerR * 1.03, width: outerR * 2.06, height: outerR * 2.06))
            context.stroke(glow2, with: .color(.neonCyan.opacity(0.15)), lineWidth: s * 0.03)

            let glow3 = Path(ellipseIn: CGRect(x: cx - outerR * 1.01, y: cy - outerR * 1.01, width: outerR * 2.02, height: outerR * 2.02))
            context.stroke(glow3, with: .color(.neonCyan.opacity(0.30)), lineWidth: s * 0.025)

            // Interior
            let outerCircle = Path(ellipseIn: CGRect(x: cx - outerR, y: cy - outerR, width: outerR * 2, height: outerR * 2))
            context.fill(outerCircle, with: .color(.gaugeInterior))
            context.stroke(outerCircle, with: .color(.neonCyan), lineWidth: s * 0.02)

            // Inner ring
            let innerR = s * 0.30
            let innerCircle = Path(ellipseIn: CGRect(x: cx - innerR, y: cy - innerR, width: innerR * 2, height: innerR * 2))
            context.fill(innerCircle, with: .color(.gaugeInterior))
            context.stroke(innerCircle, with: .color(.neonCyan.opacity(0.4)), lineWidth: s * 0.008)

            // Center dot
            let centerDotR = s * 0.012
            let centerDot = Path(ellipseIn: CGRect(x: cx - centerDotR, y: cy - centerDotR, width: centerDotR * 2, height: centerDotR * 2))
            context.fill(centerDot, with: .color(.neonCyan.opacity(0.6)))

            // Major ticks (8, every 45°)
            for i in 0..<8 {
                let angle = Double(i) * .pi / 4.0
                let tickStart = outerR - s * 0.06
                let tickEnd = outerR - s * 0.002
                context.stroke(
                    Path { p in
                        p.move(to: CGPoint(x: cx + cos(angle) * tickStart, y: cy + sin(angle) * tickStart))
                        p.addLine(to: CGPoint(x: cx + cos(angle) * tickEnd, y: cy + sin(angle) * tickEnd))
                    },
                    with: .color(.neonCyan.opacity(0.6)),
                    lineWidth: s * 0.008
                )
            }

            // Minor ticks (8, offset 22.5°)
            for i in 0..<8 {
                let angle = (Double(i) * 45.0 + 22.5) * .pi / 180.0
                let tickStart = outerR - s * 0.03
                let tickEnd = outerR - s * 0.002
                context.stroke(
                    Path { p in
                        p.move(to: CGPoint(x: cx + cos(angle) * tickStart, y: cy + sin(angle) * tickStart))
                        p.addLine(to: CGPoint(x: cx + cos(angle) * tickEnd, y: cy + sin(angle) * tickEnd))
                    },
                    with: .color(.neonCyan.opacity(0.3)),
                    lineWidth: s * 0.005
                )
            }

            // Moving dot
            let clampedX = max(-gravity, min(gravity, x))
            let clampedY = max(-gravity, min(gravity, y))
            let px = cx + CGFloat(-clampedX / gravity) * outerR
            let py = cy + CGFloat(clampedY / gravity) * outerR
            let dotR = s * 0.025

            // Dot glow
            let halo = Path(ellipseIn: CGRect(x: px - dotR * 3, y: py - dotR * 3, width: dotR * 6, height: dotR * 6))
            context.fill(halo, with: .color(.neonCyan.opacity(0.15)))
            let innerGlow = Path(ellipseIn: CGRect(x: px - dotR * 1.8, y: py - dotR * 1.8, width: dotR * 3.6, height: dotR * 3.6))
            context.fill(innerGlow, with: .color(.neonCyan.opacity(0.40)))
            let dot = Path(ellipseIn: CGRect(x: px - dotR, y: py - dotR, width: dotR * 2, height: dotR * 2))
            context.fill(dot, with: .color(.neonCyan))
        }
    }
}
