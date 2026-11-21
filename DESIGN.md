# MAGIX Mobile Design System Specification (Stitch UI Design System)

**Product:** MAGIX — Personal AI Assistant for Android  
**Version:** Phase 1 Design System (Stitch Mobile UI Reference)  
**Aesthetic Intent:** Futuristic, calm, sophisticated, cinematic, technically advanced, trustworthy.  
**Strict Anti-Patterns:** No cyberpunk neon clutter, no childish game aesthetics, no generic ChatGPT/messenger clones. High-end obsidian luxury hardware integration.

---

## 1. Color Palette & Tokens

### 1.1 Foundation & Surfaces (OLED-optimized Dark Architecture)
- **Background Deep / Void:** `#080B11` (99.5% black with deep cosmic undertone)
- **Surface Level 0 (Base Canvas):** `#0D111A`
- **Surface Level 1 (Card & Section Containers):** `#141A26`
- **Surface Level 2 (Elevated Panels, Modals):** `#1B2232`
- **Surface Level 3 (Active / Focused Container):** `#232C40`
- **Surface Border (Subtle Graphite Inset):** `#222D3D` (40% opacity on standard cards)
- **Surface Border Glow (Active/Hover):** `#3A86FF33` (20% cool blue glow)

### 1.2 Accent Lighting (Restrained Ice-Blue / Cyan)
- **Primary Accent (Quantum Cyan):** `#38BDF8` (Sky 400 / Ice Cyan)
- **Secondary Accent (Cosmic Blue):** `#2563EB` (Restrained Deep Blue)
- **Core Luminescence (Glow Highlights):** `#E0F2FE` (Ice White Spark)
- **Subtle Ambient Radial Glow:** `rgba(56, 189, 248, 0.08)` to `rgba(37, 99, 235, 0.03)`

### 1.3 Semantic Indicators
- **Ready / Success:** `#34D399` (Emerald Mint - subdued luxury)
- **Warning / Attention:** `#FBBF24` (Amber Warmth)
- **Error / Interruption:** `#F87171` (Crimson Rose - soft, not screaming red)
- **Executing / Action:** `#A78BFA` (Amethyst Pulse)

### 1.4 Typography Tokens (Silver / White Scale)
- **Text Primary (Display / Headers):** `#F8FAFC` (Slate 50 - pristine high contrast)
- **Text Secondary (Subtitles / Captions):** `#94A3B8` (Slate 400 - calm legibility)
- **Text Tertiary (Metadata / Timestamps):** `#64748B` (Slate 500)
- **Text Accent (Status Pills, Tags):** `#7DD3FC` (Ice Blue)

---

## 2. Typography Hierarchy

Using Android system font scaling (`sp`), geometric sans modern weights:
- **Display Large (Hero Greetings):** 28sp, Bold (Weight 700), Letter-spacing -0.5sp, Line-height 34sp
- **Display Medium (Assistant Readiness):** 22sp, SemiBold (Weight 600), Letter-spacing -0.25sp
- **Title Large (Screen Headings):** 20sp, SemiBold (Weight 600), Letter-spacing 0sp
- **Title Medium (Card Titles / Routine Headers):** 16sp, Medium (Weight 500), Letter-spacing 0.15sp
- **Body Large (Assistant Responses, Dialogue):** 15sp, Normal (Weight 400), Letter-spacing 0.25sp, Line-height 22sp
- **Body Medium (Settings Labels, Input):** 14sp, Normal (Weight 400), Letter-spacing 0.2sp
- **Label Medium (Buttons, Action Pills):** 13sp, SemiBold (Weight 600), Letter-spacing 0.4sp
- **Label Small (Tags, Timestamp, Status):** 11sp, Medium (Weight 500), Letter-spacing 0.5sp, Uppercase

---

## 3. Spacing & Dimensional Grid

Strict 8dp modular grid system:
- `spacing_xxs`: 4dp
- `spacing_xs`: 8dp
- `spacing_sm`: 12dp
- `spacing_md`: 16dp (Standard screen margin & inner card padding)
- `spacing_lg`: 24dp (Section gap)
- `spacing_xl`: 32dp (Header to Core offset)
- `spacing_xxl`: 48dp

Minimum Touch Target: Strict **48dp × 48dp** for all interactive components (`minimumInteractiveComponentSize`).

---

## 4. Corner Radii & Elevation

- **Small Controls (Chips, Badges):** 8dp
- **Medium Inputs & Buttons:** 16dp
- **Cards & Modules:** 20dp
- **Core Orb Container:** Circular (50% / CircleShape)
- **Floating Input Dock:** 28dp pill shape
- **Bottom Navigation Bar:** 24dp top-rounded corners, floating dock elevation

---

## 5. Surfaces & Glass Effects

- **Graphite Translucent Surface:** Background `#141A26` with 85% alpha + 1dp border `#243046`.
- **Cosmic Card:** Linear gradient from `#141B29` (top left) to `#0F1420` (bottom right).
- **Core Stage:** Radial background spotlight with 120dp radius `RadialGradient(colors = [#152A42, #080B11])`.

---

## 6. Buttons & Interactive Controls

- **Primary Action (Quantum Glow Button):** Ice-blue filled or high-contrast silver container with subtle cyan shadow, ripple feedback.
- **Secondary Action (Ghost Border):** Border 1dp `#2E3D52`, container `#141A26` with 60% opacity.
- **Microphone Pill / Core Trigger:** 64dp circle or thumb-docked pill with breathing outer ring.
- **Segmented Selectors (Voice Profile, Voice Speed):** Pill container `#141B26` with animated sliding selector indicator `#25354D`.

---

## 7. Cards & Layout Modules

- **Status Card:** Displays real-time readiness, battery-conscious state, network/permission status.
- **Routine / Automation Card:** Title, schedule badge, description, toggle switch, and multi-step action breakdown pill.
- **Memory Card:** Category badge (`Preferences`, `Routines`, `Work`), memory text, created timestamp, swipe/action delete icon button.
- **Settings Row Card:** Icon container (graphite rounded square), title, status subtitle ("Male Voice / Puck", "1.0x", "Connected"), chevron or action control.

---

## 8. Icons & Navigation System

- **Bottom Navigation Bar:**
  - 5 Fixed Destinations:
    1. **Home** (`Home` / `Radar`)
    2. **Chat** (`ChatBubble` / `AutoAwesome`)
    3. **Automations** (`Schedule` / `ElectricBolt`)
    4. **Memory** (`Psychology` / `Memory`)
    5. **Settings** (`Tune` / `Settings`)
  - Elevated floating bar docked at bottom with full window insets handling (`navigationBarsPadding`).
  - Active pill: `#38BDF8` accent tint with glow indicator dot.

---

## 9. MAGIX Core States (Visual Identity & Animation Engine)

The MAGIX Core is the centerpiece of the intelligence layer. Rendered with custom Jetpack Compose Canvas:

1. **IDLE:**
   - Visual: Concentric orbital rings with 4 satellite quantum nodes; central glowing orb breathing at 0.15 Hz (gentle sine curve scale 0.95x - 1.05x).
   - Color: Cool slate / cyan `#38BDF8` with 40% glow opacity.
2. **LISTENING:**
   - Visual: Expanded pulsating waveform audio-reactive rings that swell outwards; outer ring ripple effects; high frequency responsive vibrations.
   - Color: Bright cyan / sky blue `#00E5FF` & `#80D8FF`.
3. **THINKING:**
   - Visual: Synchronous dual-orbital clockwise and counter-clockwise rotational rings with particle tracer pulses orbiting the nucleus.
   - Color: Deep cyan to royal sapphire gradient (`#38BDF8` -> `#6366F1`).
4. **SPEAKING:**
   - Visual: Harmonic audio-visualizer frequency bars radiating vertically through the core sphere; smooth organic fluid breathing.
   - Color: Radiant ice blue `#E0F2FE` with electric cyan fringes.
5. **EXECUTING:**
   - Visual: High-velocity orbital spin with targeted directional vector ticks, representing autonomous task orchestration.
   - Color: Soft Amethyst / Cyber Purple `#A78BFA` with cyan spark.
6. **SUCCESS:**
   - Visual: Radiant outward shockwave flash that settles into a crystalline ring with a centered emerald check aura.
   - Color: Emerald Mint `#34D399` halo.
7. **ERROR:**
   - Visual: Rapid damped harmonic oscillation (gentle horizontal jitter) with subdued crimson flare.
   - Color: Rose Amber `#F87171`.

---

## 10. Conversation & Chat Experience

- **User Bubble:** Right-aligned, graphite surface `#1E2638`, border `#2D3950`, crisp slate typography.
- **MAGIX Bubble:** Left-aligned, dark glass `#121824`, subtle cyan accent left edge rail, formatted message content, audio replay placeholder button, assistant state badge.
- **Development Mock Banner:** Prominently displays: `"MAGIX Core Dev Mock Engine · Phase 1 Architecture"`. Transparent, honest, clean.
- **Input Bar:** Floating docked input capsule with microphone trigger, send button, and text field with auto-expansion up to 4 lines.

---

## 11. Automations Screen

- **Hero Header:** "Active Routines & Subroutines".
- **Quick Status:** "3 Autonomous Workflows Configured (Phase 1 Local Mock Engine)".
- **Cards:**
  - *Morning Briefing Routine* (07:00 AM Daily)
  - *Deep Work Focus Protocol* (App launch trigger)
  - *Evening Wind-Down* (10:30 PM Daily)
- **Card Elements:** Action sequence preview chips (e.g. `Weather` -> `Calendar` -> `Commute Brief`), Toggle switch with active cyan state.

---

## 12. Memory Center Screen

- **Concept:** Transparent personal context bank that later phases hook into vector embeddings.
- **Categories:** All, Preferences, Routines, Personal.
- **Capabilities:**
  - View all stored memories with metadata.
  - Add new memory via modal or quick card.
  - Delete individual memories with instant local repository sync.
  - Empty state with guidance on how MAGIX remembers context.

---

## 13. Settings & Voice Profiles

- **Voice Profile Selection:**
  - **MAGIX Male** (Internal ID: `Puck`) — "Confident, energetic and intelligent"
  - **MAGIX Female** (Internal ID: `Kore`) — "Calm, precise and sophisticated"
  - Interactive audition playback preview button (tone simulation).
- **Voice Speed:**
  - Discrete chips: `0.75x`, `1.0x` (Default), `1.25x`, `1.5x`.
- **System Permissions Status:**
  - Microphone: Real-time Android runtime check (`Connected` / `Not Connected`).
  - Notifications: Real-time Android runtime check (`Connected` / `Not Connected`).
  - Accessibility: Transparently marked `"Not Connected (Phase 2+ Engine)"`.
- **Deferred Systems Notice:**
  - Clearly tagged with `"Coming Soon"` or `"Not Connected"` for honest transparency.

---

## 14. Responsive & Mobile Ergonomics

- **Thumb Zone Compliance:** Primary interactive hubs (Mic trigger, Core tap, bottom tabs, action buttons) are situated within the bottom 60% of the screen viewport.
- **Window Insets:** Zero clipping on punch-hole cameras, notches, or gesture pills (`safeDrawing` & `navigationBarsPadding`).
- **One-Handed Reach:** Key titles and greetings feature generous top margins that collapse naturally on scroll.
