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
            let outerR = s * 0.46
            let innerR = s * 0.30

            // Outer ring
            let outerCircle = Path(ellipseIn: CGRect(x: cx - outerR, y: cy - outerR, width: outerR * 2, height: outerR * 2))
            context.fill(outerCircle, with: .color(.gray.opacity(0.1)))
            context.stroke(outerCircle, with: .color(.secondary), lineWidth: s * 0.02)

            // Inner ring
            let innerCircle = Path(ellipseIn: CGRect(x: cx - innerR, y: cy - innerR, width: innerR * 2, height: innerR * 2))
            context.fill(innerCircle, with: .color(.gray.opacity(0.05)))
            context.stroke(innerCircle, with: .color(.secondary), lineWidth: s * 0.01)

            // Center dot
            let centerDotR = s * 0.015
            let centerDot = Path(ellipseIn: CGRect(x: cx - centerDotR, y: cy - centerDotR, width: centerDotR * 2, height: centerDotR * 2))
            context.fill(centerDot, with: .color(.secondary))

            // Moving point
            let clampedX = max(-gravity, min(gravity, x))
            let clampedY = max(-gravity, min(gravity, y))
            let px = cx + CGFloat(-clampedX / gravity) * outerR
            let py = cy + CGFloat(clampedY / gravity) * outerR
            let dotR = s * 0.03
            let dot = Path(ellipseIn: CGRect(x: px - dotR, y: py - dotR, width: dotR * 2, height: dotR * 2))
            context.fill(dot, with: .color(.orange))
        }
    }
}
