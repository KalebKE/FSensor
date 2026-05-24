import SwiftUI

struct RotationGaugeView: View {
    let heading: Float
    let pitch: Float
    let roll: Float

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

            // Tick marks on bezel (4 cardinal + 4 intercardinal)
            for i in 0..<8 {
                let angle = Double(i) * .pi / 4.0 - .pi / 2.0
                let isCardinal = i % 2 == 0
                let tickLen = isCardinal ? s * 0.06 : s * 0.04
                let alpha = isCardinal ? 0.7 : 0.4
                let tickStart = outerR + s * 0.005
                let tickEnd = tickStart + tickLen
                context.stroke(
                    Path { p in
                        p.move(to: CGPoint(x: cx + cos(angle) * tickStart, y: cy + sin(angle) * tickStart))
                        p.addLine(to: CGPoint(x: cx + cos(angle) * tickEnd, y: cy + sin(angle) * tickEnd))
                    },
                    with: .color(.neonCyan.opacity(alpha)),
                    lineWidth: s * 0.008
                )
            }

            // Clip to circle
            let clipRect = CGRect(x: cx - outerR, y: cy - outerR, width: outerR * 2, height: outerR * 2)
            let clipCircle = Path(ellipseIn: clipRect)

            let rollDeg = -Double(roll) * 180.0 / .pi
            let pitchOffset = CGFloat(pitch / (.pi / 2)) * outerR

            context.clipToLayer(opacity: 1) { ctx in
                ctx.fill(clipCircle, with: .color(.white))
            }

            // Sky + ground with rotation
            var horizonContext = context
            horizonContext.translateBy(x: cx, y: cy)
            horizonContext.rotate(by: .degrees(rollDeg))
            horizonContext.translateBy(x: -cx, y: -cy)

            let horizonY = cy + pitchOffset
            horizonContext.fill(
                Path(CGRect(x: 0, y: 0, width: size.width, height: horizonY)),
                with: .color(.rotationSky)
            )
            horizonContext.fill(
                Path(CGRect(x: 0, y: horizonY, width: size.width, height: size.height - horizonY)),
                with: .color(.rotationGround)
            )
            horizonContext.stroke(
                Path { p in
                    p.move(to: CGPoint(x: 0, y: horizonY))
                    p.addLine(to: CGPoint(x: size.width, y: horizonY))
                },
                with: .color(.neonCyan.opacity(0.7)),
                lineWidth: s * 0.006
            )

            // Main rim
            context.stroke(clipCircle, with: .color(.neonCyan), lineWidth: s * 0.02)

            // Center reference
            context.stroke(
                Path { p in
                    p.move(to: CGPoint(x: cx - s * 0.08, y: cy))
                    p.addLine(to: CGPoint(x: cx + s * 0.08, y: cy))
                },
                with: .color(.neonCyan.opacity(0.8)),
                lineWidth: s * 0.006
            )
            let refDotR = s * 0.012
            context.stroke(
                Path(ellipseIn: CGRect(x: cx - refDotR, y: cy - refDotR, width: refDotR * 2, height: refDotR * 2)),
                with: .color(.neonCyan.opacity(0.8)),
                lineWidth: s * 0.004
            )
        }
    }
}
