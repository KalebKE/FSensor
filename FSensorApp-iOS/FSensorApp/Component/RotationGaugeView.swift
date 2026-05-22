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

            // Clip to circle
            let clipRect = CGRect(x: cx - outerR, y: cy - outerR, width: outerR * 2, height: outerR * 2)
            let clipPath = Path(ellipseIn: clipRect)

            let rollDeg = -Double(roll) * 180.0 / .pi
            let pitchOffset = CGFloat(pitch / (.pi / 2)) * outerR

            context.clipToLayer(opacity: 1) { ctx in
                ctx.fill(clipPath, with: .color(.white))
            }

            // Draw sky + ground with rotation
            var horizonContext = context
            horizonContext.translateBy(x: cx, y: cy)
            horizonContext.rotate(by: .degrees(rollDeg))
            horizonContext.translateBy(x: -cx, y: -cy)

            let horizonY = cy + pitchOffset
            // Sky
            horizonContext.fill(
                Path(CGRect(x: 0, y: 0, width: size.width, height: horizonY)),
                with: .color(Color(red: 0.55, green: 0.77, blue: 0.95))
            )
            // Ground
            horizonContext.fill(
                Path(CGRect(x: 0, y: horizonY, width: size.width, height: size.height - horizonY)),
                with: .color(Color(red: 0.55, green: 0.31, blue: 0.17))
            )
            // Horizon line
            horizonContext.stroke(
                Path { p in
                    p.move(to: CGPoint(x: 0, y: horizonY))
                    p.addLine(to: CGPoint(x: size.width, y: horizonY))
                },
                with: .color(.white),
                lineWidth: s * 0.005
            )

            // Rim
            context.stroke(clipPath, with: .color(.secondary), lineWidth: s * 0.02)

            // Center reference
            context.stroke(
                Path { p in
                    p.move(to: CGPoint(x: cx - s * 0.08, y: cy))
                    p.addLine(to: CGPoint(x: cx + s * 0.08, y: cy))
                },
                with: .color(.white),
                lineWidth: s * 0.006
            )
            let refDotR = s * 0.012
            context.stroke(
                Path(ellipseIn: CGRect(x: cx - refDotR, y: cy - refDotR, width: refDotR * 2, height: refDotR * 2)),
                with: .color(.white),
                lineWidth: s * 0.004
            )
        }
    }
}
