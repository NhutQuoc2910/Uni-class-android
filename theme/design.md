---
version: alpha
name: UniClass-Nordic-Light-Design-System
description: An architectural EdTech interface built on the Nordic Slate 6-color system in Nordic Light mode. Deep Obsidian-to-Slate hero cards (#06141B → #11212D → #253745) and high-contrast slate CTAs anchor an airy Nordic Tint canvas (#F4F6F6). Framed by geometric Plus Jakarta Sans headlines, JetBrains Mono verification tokens, Material 3 floating-label inputs, and thumb-zone bottom ergonomics designed for university classrooms and real-time QR attendance.

colors:
  obsidian-navy: "#06141B"
  midnight-slate: "#11212D"
  rich-slate-blue: "#253745"
  muted-steel: "#4A5C6A"
  pewter-mist: "#9BA8AB"
  frosted-silver: "#CCD0CF"
  canvas-nordic-light: "#F4F6F6"
  surface-pure-white: "#FFFFFF"
  surface-frosted-80: "#CCCCD0CF"
  surface-frosted-65: "#A6CCD0CF"
  surface-frosted-55: "#8CCCD0CF"
  surface-frosted-45: "#73CCD0CF"
  surface-frosted-35: "#59CCD0CF"
  brand-uc-accent: "#3B82F6"
  error-rose: "#F43F5E"
  error-rose-ring: "#33F43F5E"
  on-dark-white: "#FFFFFF"
  on-dark-silver: "#CCD0CF"
  on-dark-muted: "#9BA8AB"
  on-light-primary: "#06141B"
  on-light-secondary: "#4A5C6A"
  on-light-muted: "#9BA8AB"
  focus-ring-slate: "#1A253745"
  otp-active-ring: "#26253745"

typography:
  hero-brand-title:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: 20px
    fontWeight: 800
    lineHeight: 1.2
    letterSpacing: -0.4px
  screen-headline:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: 20px
    fontWeight: 800
    lineHeight: 1.25
    letterSpacing: -0.4px
  hero-greeting:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: 18px
    fontWeight: 800
    lineHeight: 1.35
    letterSpacing: -0.25px
  role-card-title:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: 16px
    fontWeight: 800
    lineHeight: 1.3
    letterSpacing: -0.16px
  otp-digit-display:
    fontFamily: "JetBrains Mono, monospace"
    fontSize: 18px
    fontWeight: 800
    lineHeight: 1.0
    letterSpacing: 0
  button-cta-primary:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: 14px
    fontWeight: 800
    lineHeight: 1.0
    letterSpacing: 0
  input-value:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: 14px
    fontWeight: 600
    lineHeight: 1.4
    letterSpacing: 0
  input-floating-label:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: 14px
    fontWeight: 500
    lineHeight: 1.0
    letterSpacing: 0
  input-floating-label-shrunk:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: 11px
    fontWeight: 700
    lineHeight: 1.0
    letterSpacing: 0.1px
  top-bar-title:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: 12px
    fontWeight: 700
    lineHeight: 1.2
    letterSpacing: -0.2px
  body-subtitle:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: 12px
    fontWeight: 400
    lineHeight: 1.5
    letterSpacing: 0
  body-strong:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: 12px
    fontWeight: 700
    lineHeight: 1.4
    letterSpacing: 0
  mono-email-value:
    fontFamily: "JetBrains Mono, monospace"
    fontSize: 12px
    fontWeight: 700
    lineHeight: 1.3
    letterSpacing: 0
  chip-feature:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: 11px
    fontWeight: 600
    lineHeight: 1.2
    letterSpacing: 0
  badge-pill:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: 11px
    fontWeight: 700
    lineHeight: 1.0
    letterSpacing: 0.2px
  micro-uppercase:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: 10px
    fontWeight: 700
    lineHeight: 1.0
    letterSpacing: 0.8px

rounded:
  none: 0px
  sm: 8px
  md: 12px
  inner-icon: 14px
  lg: 16px
  xl: 24px
  pill: 9999px
  full: 9999px

spacing:
  xxs: 4px
  xs: 6px
  sm: 8px
  md: 12px
  lg: 16px
  xl: 20px
  xxl: 24px
  section: 32px

components:
  top-app-bar:
    backgroundColor: "{colors.canvas-nordic-light}"
    textColor: "{colors.midnight-slate}"
    typography: "{typography.top-bar-title}"
    height: 44px
    padding: 4px 16px 12px 16px
  progress-segment-active:
    backgroundColor: "{colors.midnight-slate}"
    rounded: "{rounded.pill}"
    height: 6px
  progress-segment-idle:
    backgroundColor: "{colors.frosted-silver}"
    rounded: "{rounded.pill}"
    height: 6px
  step-badge-pill:
    backgroundColor: "{colors.surface-frosted-80}"
    textColor: "{colors.midnight-slate}"
    typography: "{typography.badge-pill}"
    rounded: "{rounded.pill}"
    padding: 4px 10px
  hero-header-card:
    backgroundColor: "{colors.midnight-slate}"
    textColor: "{colors.on-dark-silver}"
    typography: "{typography.hero-greeting}"
    rounded: "{rounded.xl}"
    padding: 20px
  hero-qr-live-badge:
    backgroundColor: "{colors.obsidian-navy}"
    textColor: "{colors.on-dark-silver}"
    typography: "{typography.badge-pill}"
    rounded: "{rounded.md}"
    padding: 6px 10px
  button-primary-cta:
    backgroundColor: "{colors.midnight-slate}"
    textColor: "{colors.on-dark-silver}"
    typography: "{typography.button-cta-primary}"
    rounded: "{rounded.lg}"
    height: 54px
  button-primary-cta-active:
    backgroundColor: "{colors.obsidian-navy}"
    textColor: "{colors.on-dark-white}"
    rounded: "{rounded.lg}"
    height: 54px
  button-secondary-action:
    backgroundColor: "{colors.midnight-slate}"
    textColor: "{colors.on-dark-silver}"
    typography: "{typography.body-strong}"
    rounded: "{rounded.md}"
    padding: 8px 14px
  button-surface-action:
    backgroundColor: "{colors.surface-pure-white}"
    textColor: "{colors.midnight-slate}"
    typography: "{typography.body-strong}"
    rounded: "{rounded.md}"
    padding: 6px 10px
  button-icon-back:
    backgroundColor: transparent
    textColor: "{colors.obsidian-navy}"
    rounded: "{rounded.full}"
    size: 40px
  m3-input-field:
    backgroundColor: "{colors.surface-pure-white}"
    textColor: "{colors.on-light-primary}"
    typography: "{typography.input-value}"
    rounded: "{rounded.lg}"
    height: 56px
    padding: 20px 44px 6px 46px
  m3-input-field-focused:
    backgroundColor: "{colors.surface-pure-white}"
    textColor: "{colors.on-light-primary}"
    rounded: "{rounded.lg}"
    height: 56px
  m3-input-field-error:
    backgroundColor: "{colors.surface-pure-white}"
    textColor: "{colors.error-rose}"
    rounded: "{rounded.lg}"
    height: 56px
  register-prompt-card:
    backgroundColor: "{colors.surface-frosted-45}"
    textColor: "{colors.on-light-primary}"
    typography: "{typography.body-strong}"
    rounded: "{rounded.lg}"
    padding: 14px
  role-selection-card-selected:
    backgroundColor: "{colors.surface-pure-white}"
    textColor: "{colors.on-light-primary}"
    typography: "{typography.role-card-title}"
    rounded: "{rounded.xl}"
    padding: 16px
  role-selection-card-idle:
    backgroundColor: "{colors.surface-frosted-35}"
    textColor: "{colors.on-light-primary}"
    typography: "{typography.role-card-title}"
    rounded: "{rounded.xl}"
    padding: 16px
  role-feature-chip:
    backgroundColor: "{colors.surface-frosted-65}"
    textColor: "{colors.midnight-slate}"
    typography: "{typography.chip-feature}"
    rounded: "{rounded.sm}"
    padding: 4px 10px
  role-context-pill:
    backgroundColor: "{colors.surface-frosted-55}"
    textColor: "{colors.on-light-primary}"
    typography: "{typography.body-strong}"
    rounded: "{rounded.lg}"
    padding: 10px 14px
  highlighted-email-card:
    backgroundColor: "{colors.surface-frosted-65}"
    textColor: "{colors.on-light-primary}"
    typography: "{typography.mono-email-value}"
    rounded: "{rounded.lg}"
    padding: 14px
  otp-digit-box-idle:
    backgroundColor: "{colors.surface-pure-white}"
    textColor: "{colors.on-light-primary}"
    typography: "{typography.otp-digit-display}"
    rounded: "{rounded.lg}"
    size: 48px 56px
  otp-digit-box-active:
    backgroundColor: "{colors.surface-pure-white}"
    textColor: "{colors.on-light-primary}"
    typography: "{typography.otp-digit-display}"
    rounded: "{rounded.lg}"
    size: 48px 56px
  otp-digit-box-filled:
    backgroundColor: "{colors.surface-frosted-45}"
    textColor: "{colors.on-light-primary}"
    typography: "{typography.otp-digit-display}"
    rounded: "{rounded.lg}"
    size: 48px 56px
  otp-digit-box-verified:
    backgroundColor: "{colors.rich-slate-blue}"
    textColor: "{colors.on-dark-silver}"
    typography: "{typography.otp-digit-display}"
    rounded: "{rounded.lg}"
    size: 48px 56px
  otp-auto-verify-banner:
    backgroundColor: "{colors.midnight-slate}"
    textColor: "{colors.on-dark-silver}"
    typography: "{typography.body-strong}"
    rounded: "{rounded.lg}"
    height: 54px
    padding: 14px
  security-requirement-chip-active:
    backgroundColor: "{colors.midnight-slate}"
    textColor: "{colors.on-dark-silver}"
    typography: "{typography.badge-pill}"
    rounded: "{rounded.md}"
    padding: 4px 10px
  security-requirement-chip-idle:
    backgroundColor: "{colors.surface-frosted-65}"
    textColor: "{colors.muted-steel}"
    typography: "{typography.badge-pill}"
    rounded: "{rounded.md}"
    padding: 4px 10px
  hint-info-card:
    backgroundColor: "{colors.surface-frosted-55}"
    textColor: "{colors.midnight-slate}"
    typography: "{typography.chip-feature}"
    rounded: "{rounded.lg}"
    padding: 14px
  floating-snackbar:
    backgroundColor: "{colors.obsidian-navy}"
    textColor: "{colors.on-dark-silver}"
    typography: "{typography.body-strong}"
    rounded: "{rounded.lg}"
    padding: 12px 16px
---

## Overview

UniClass's redesigned mobile interface establishes an **architectural Nordic Light EdTech aesthetic** built strictly from a disciplined 6-color Nordic Slate palette (`#06141B`, `#11212D`, `#253745`, `#4A5C6A`, `#9BA8AB`, `#CCD0CF`) resting on an ultra-clean Nordic Tint canvas (`#F4F6F6`). Instead of generic saturated blues, visual authority comes from deep slate-navy surfaces juxtaposed against crisp pure-white form controls and translucent frosted-silver containers.

Every screen is engineered around **one-handed mobile ergonomics (Thumb-Zone first)** and **progressive disclosure**. Long, overwhelming forms are broken into focused, single-purpose viewport stages: Role Selection (`Bước 1/2`) is decoupled from Account Information (`Bước 2/2`), OTP verification auto-triggers the moment the 6th digit lands, and password rules validate live through interactive security chips rather than post-submit error dialogs.

Across all flows, typography pairs geometric **Plus Jakarta Sans** at heavy display weights (`700–800`) with **JetBrains Mono** for high-precision academic data (OTP digits, student email addresses, step counters). The result is a university platform that feels both academically trustworthy and modern.

**Key Characteristics:**
- **Nordic Light Contrast Architecture:** A cool mist canvas (`{colors.canvas-nordic-light}` — `#F4F6F6`) paired with pure white interactive surfaces (`#FFFFFF`) and anchored by deep obsidian-to-slate hero cards and primary CTAs (`#06141B → #11212D → #253745`).
- **Strict 6-Token Color Discipline:** Every surface, border, icon, and badge maps back to the 6 Nordic Slate swatches. Error validation (`#F43F5E`) and the U-C emblem accent (`#3B82F6`) are the only external semantic signals.
- **Material 3 Floating-Label Input Grammar:** 56dp height rounded-2xl (`16dp`) fields with leading icons, internal floating labels that scale to `0.76x` at `top: 9dp` on focus/fill, and inline error feedback with horizontal shake micro-animation.
- **Zero-Friction Auto-Verification:** 6-box monospace OTP grid automatically locks and triggers verification immediately upon the 6th digit or clipboard paste, paired with a dashed email verification card featuring an inline **"Sửa"** (Edit) action.
- **Sleek Segmented Progress Navigation:** Traditional bulky numbered circle steppers are replaced by a compact 44dp Top App Bar + 6dp horizontal segmented progress bar (`{component.progress-segment-active}` vs `{component.progress-segment-idle}`).
- **Live Security Requirement Chips:** Real-time password validation chips transition smoothly from frosted silver (`{component.security-requirement-chip-idle}`) to deep midnight slate (`{component.security-requirement-chip-active}`) as each character rule is satisfied.

## Colors

> **Source screens analyzed:** Login (`login`), Register Step 1 Role Selection (`register_role`), Register Step 2 Account Form (`register_form`), OTP Auto-Verification (`verify_otp`), Forgot Password Step 1 Email (`forgot_email`), and Forgot Password Step 3 New Password (`forgot_reset`) in **Nordic Light** mode.

### Core Nordic Slate 6-Color System
- **Obsidian Navy** (`{colors.obsidian-navy}` — `#06141B`): The deepest anchor tone. Used for primary screen headlines, typed input text, the start stop of the Hero Header and Primary CTA gradients, status bar icons, and floating snackbar surfaces.
- **Midnight Slate** (`{colors.midnight-slate}` — `#11212D`): The primary interactive brand color in Nordic Light mode. Powers active card borders (`2dp`), active progress bar segments, selected role radio indicators, active security requirement chips, and solid secondary buttons.
- **Rich Slate Blue** (`{colors.rich-slate-blue}` — `#253745`): The mid-dark architectural tone. Used as the terminal stop of the Hero Header gradient, verified OTP box fills, role popularity badges (*"Phổ biến"*), and icon containers on forgot-password headers.
- **Muted Steel** (`{colors.muted-steel}` — `#4A5C6A`): The workhorse secondary tone. Used for unfocused input leading/trailing icons, floating labels at rest, screen subtitles, dashed border strokes on the highlighted email box, and idle security chip text.
- **Pewter Mist** (`{colors.pewter-mist}` — `#9BA8AB`): The structural border and muted token. Used for resting input borders (`80%` alpha), unselected role radio outlines, section divider captions (*"HOẶC"*), and subtle ambient glow orbs inside the Hero card.
- **Frosted Silver** (`{colors.frosted-silver}` — `#CCD0CF`): The signature high-craft light neutral. Used for text and icons sitting atop dark slate surfaces (`#06141B` / `#11212D` / `#253745`), inactive progress bar segments, card internal dividers, and translucent tinted containers across the light canvas.

### Surface & Canvas
- **Nordic Light Canvas** (`{colors.canvas-nordic-light}` — `#F4F6F6`): The global screen background across all authentication activities. Softens eye strain compared to `#FFFFFF` while making pure-white input fields and selected cards pop without heavy drop shadows.
- **Pure White Surface** (`{colors.surface-pure-white}` — `#FFFFFF`): Reserved for active/interactive surfaces: Material 3 input boxes, selected Role Cards, OTP digit boxes at rest/focus, and the security criteria card container.
- **Frosted Silver Translucent Surfaces** (`{colors.surface-frosted-35}` to `{colors.surface-frosted-80}`): Applied at calibrated opacities over `#F4F6F6` to create layered tonal hierarchy without introducing new hues:
  - `80%` (`#CCCCD0CF`): Top bar step badge (`Bước 1/2`, `Bước 3/3`).
  - `65%` (`#A6CCD0CF`): Highlighted OTP Email Card fill, role feature chips (`📷 Quét QR điểm danh`), and idle password requirement chips.
  - `55%` (`#8CCCD0CF`): Role Context Pill (`Vai trò: 🎓 Sinh viên`) and academic hint info cards (`💡`).
  - `45%` (`#73CCD0CF`): Register Prompt Card at the bottom of Login and filled OTP digit boxes.
  - `35%` (`#59CCD0CF`): Unselected Role Card background.

### Text & Iconography
- **On-Light Primary** (`{colors.on-light-primary}` — `#06141B`): All primary headings, form input values, role titles, and email addresses on light surfaces.
- **On-Light Secondary** (`{colors.on-light-secondary}` — `#4A5C6A`): Descriptive subtitles, input labels at rest, checkbox labels (*"Duy trì đăng nhập"*), and secondary helper copy.
- **On-Dark White** (`{colors.on-dark-white}` — `#FFFFFF`): High-emphasis titles inside the dark Hero Card (*"UniClass"*, *"Chào mừng bạn trở lại! 👋"*).
- **On-Dark Silver** (`{colors.on-dark-silver}` — `#CCD0CF`): Primary CTA button labels, Hero badge text, and snackbar copy.

### Hairlines, Borders & Semantic States
- **Resting Input Border:** `1dp` solid `{colors.pewter-mist}` at `80%` alpha.
- **Focused Input / Selected Card Border:** `1.5dp–2dp` solid `{colors.midnight-slate}` (`#11212D`) with an outer `4dp` focus halo in `{colors.focus-ring-slate}` (`#1A253745`).
- **Dashed Email Verification Border:** `2dp` dashed `{colors.muted-steel}` (`#4A5C6A`).
- **Validation Error Rose** (`{colors.error-rose}` — `#F43F5E`): Applied to input borders (`2dp`), leading error icons, floating labels, and inline helper messages when validation fails.

### Brand Gradient
Unlike flat utility systems, Nordic Light uses **one signature dark architectural gradient**:
- **Nordic Slate Linear Gradient:** `135deg` from `{colors.obsidian-navy}` (`#06141B`) → `{colors.midnight-slate}` (`#11212D`) → `{colors.rich-slate-blue}` (`#253745`). Used exclusively on the Login Hero Header Card and Primary CTA buttons (`{component.button-primary-cta}`), creating an immediate visual link with the UniClass U-C Adaptive App Icon.

## Typography

### Font Family
- **Primary UI & Display**: `Plus Jakarta Sans, sans-serif` — A modern geometric sans-serif with tall x-height and crisp ink traps. Used at weights `400`, `500`, `600`, `700`, and `800`.
- **Verification & Technical Mono**: `JetBrains Mono, monospace` — Used for 6-digit OTP inputs, email verification readouts, and step progress badges where character distinction (`0` vs `O`, `1` vs `l`) is critical.

### Hierarchy

| Token | Size | Weight | Line Height | Letter Spacing | Use |
|---|---|---|---|---|---|
| `{typography.hero-brand-title}` | 20px (20sp) | 800 | 1.20 | -0.4px | Brand title *"UniClass"* inside Login Hero Header |
| `{typography.screen-headline}` | 20px (20sp) | 800 | 1.25 | -0.4px | Primary h1 on Role Selection, OTP, and Forgot Password screens |
| `{typography.hero-greeting}` | 18px (18sp) | 800 | 1.35 | -0.25px | Hero card welcome headline (*"Chào mừng bạn trở lại! 👋"*) |
| `{typography.role-card-title}` | 16px (16sp) | 800 | 1.30 | -0.16px | Role card headers (*"Sinh viên"*, *"Giảng viên"*) |
| `{typography.otp-digit-display}` | 18px (18sp) | 800 | 1.00 | 0 | Monospace digits inside the 6 OTP boxes |
| `{typography.button-cta-primary}` | 14px (14sp) | 800 | 1.00 | 0 | Full-width primary CTA button labels |
| `{typography.input-value}` | 14px (14sp) | 600 | 1.40 | 0 | User-typed value inside Material 3 inputs |
| `{typography.input-floating-label}` | 14px (14sp) | 500 | 1.00 | 0 | Input label at rest (centered vertically) |
| `{typography.input-floating-label-shrunk}` | 11px (11sp) | 700 | 1.00 | 0.1px | Input label when floated to top (`scale(0.76)`) |
| `{typography.top-bar-title}` | 12px (12sp) | 700 | 1.20 | -0.2px | Centered step title in Top App Bar |
| `{typography.body-subtitle}` | 12px (12sp) | 400 | 1.50 | 0 | Screen lead descriptions and card subtitles |
| `{typography.body-strong}` | 12px (12sp) | 700 | 1.40 | 0 | Inline links (*"Quên mật khẩu?"*, *"Gửi lại"*), snackbar copy |
| `{typography.mono-email-value}` | 12px (12sp) | 700 | 1.30 | 0 | Recipient email address inside Highlighted Email Card |
| `{typography.chip-feature}` | 11px (11sp) | 600 | 1.20 | 0 | Role capability chips & hint card body text |
| `{typography.badge-pill}` | 11px (11sp) | 700 | 1.00 | 0.2px | Step counter pills, security requirement chips, *"QR Live"* badge |
| `{typography.micro-uppercase}` | 10px (10sp) | 700 | 1.00 | 0.8px | Uppercase eyebrows (*"EMAIL NHẬN MÃ"*, *"PHỔ BIẾN"*) |

### Principles
- **ExtraBold (`800`) for structural anchors.** Headlines, role titles, and primary CTA buttons use weight `800` rather than `600`, giving compact 14sp–20sp mobile text unmistakable authority without requiring oversized font sizes that would force scrolling.
- **SemiBold (`600`) for user input values.** Typed text inside form fields runs at `14sp / 600` in `{colors.obsidian-navy}`, ensuring high legibility while floating labels sit above at `11sp / 700` in `{colors.muted-steel}`.
- **Monospace for verification data.** Every OTP digit (`{typography.otp-digit-display}`) and target email address (`{typography.mono-email-value}`) uses `JetBrains Mono` (`monospace` family in Android XML) so users can scan character-by-character before submitting.

### Note on Android XML Font Substitutes
- Bundle `plus_jakarta_sans` (`regular`, `medium`, `semibold`, `bold`, `extrabold`) and `jetbrains_mono_bold` in `res/font/` for exact parity.
- Where system fallback is used prior to font bundling, map `sans-serif-medium` + `textStyle="bold"` for `700–800` headlines and `monospace` + `textStyle="bold"` for OTP/email tokens.

## Layout

### Spacing System
- **Base unit:** `4dp` / `8dp` grid with half-step refinements (`6dp`, `10dp`, `14dp`, `20dp`) for compact mobile density.
- **Tokens:** `{spacing.xxs}` 4dp · `{spacing.xs}` 6dp · `{spacing.sm}` 8dp · `{spacing.md}` 12dp · `{spacing.lg}` 16dp · `{spacing.xl}` 20dp · `{spacing.xxl}` 24dp · `{spacing.section}` 32dp.
- **Horizontal screen padding:** `20dp` (`px-5` in prototype) across all main content viewports; `16dp` (`px-4`) on the Top App Bar.
- **Vertical input stack rhythm:** `12dp` (`space-y-3`) on 5-field registration forms; `14dp–16dp` (`space-y-3.5` / `space-y-4`) on 2-field login and password reset forms.

### Viewport & Thumb-Zone Architecture
- **Split-Vertical Flex Container (`justify-between`):** Every screen is structured as a two-part vertical container inside a `fillViewport="true"` scroll container:
  1. **Top Content Zone:** Top App Bar (with segmented progress bar) + Header/Hero + Form Controls.
  2. **Bottom Thumb-Zone Dock:** Primary CTA button (`54dp` height) + secondary prompt card or quick link, pinned with `16dp–20dp` bottom padding above the Android gesture navigation bar.
- **No-Scroll Target on Standard Phones:** By splitting Registration into Step 1 (Role Selection) and Step 2 (5 Compact Floating Inputs), every authentication screen fits comfortably within a standard `392dp × 790dp` phone viewport without vertical clutter.

### Whitespace & Step-Separation Philosophy
Cognitive load is minimized by never mixing **role context decisions** with **text entry**. On Step 1, whitespace frames two tactile Role Cards; on Step 2, the chosen role collapses into a single `44dp` `{component.role-context-pill}` at the top of the form with an inline **"Đổi vai trò"** button.

## Elevation & Depth

| Level | Treatment | Use |
|---|---|---|
| Flat Tonal | `0dp` elevation, `1dp–2dp` border | Frosted silver prompt cards, role context pill, dashed email box, idle role cards |
| Subtle Surface (`1dp–2dp`) | Soft shadow + `1dp` `{colors.pewter-mist}` border | Resting Material 3 input fields, OTP digit boxes, password criteria container |
| Selected Card (`6dp`) | `2dp` `{colors.midnight-slate}` border + `rgba(17, 33, 45, 0.10)` shadow | Active Role Selection Card, Primary CTA Button |
| Hero & Floating Overlay (`12dp`) | Deep slate gradient + `1dp` `{colors.muted-steel}` border | Login Hero Header Card, Floating Snackbar, Push OTP Notification Banner |

**Shadow philosophy.** Elevation never relies on muddy gray drop shadows alone. Every elevated surface in Nordic Light pairs its shadow with an explicit `1dp` or `2dp` structural border from the Nordic Slate palette (`#9BA8AB`, `#4A5C6A`, or `#11212D`).

### Decorative Depth
- **Ambient Blur Orbs inside Hero Card:** The Login Hero Header (`{component.hero-header-card}`) contains two subtle decorative radial circles (`#9BA8AB` at `15%` alpha top-right, `#4A5C6A` at `30%` alpha bottom-right) clipped inside its `24dp` rounded bounds.
- **Focus Ring Halos:** Focusing an input or active OTP box renders a `4dp` translucent outer ring (`{colors.otp-active-ring}` — `rgba(37, 55, 69, 0.15)`) rather than a drop shadow.

## Shapes

### Border Radius Scale

| Token | Value | Use |
|---|---|---|
| `{rounded.none}` | 0dp | Full-screen activity backgrounds |
| `{rounded.sm}` | 8dp | Role capability chips (`📷 Quét QR điểm danh`), inline `"Đổi vai trò"` button |
| `{rounded.md}` | 12dp | Secondary action buttons (`Đăng ký →`, `Sửa`), security requirement chips, `"QR Live"` badge |
| `{rounded.inner-icon}` | 14dp | Emblem container inside the Login Hero Header (`48dp × 48dp`) |
| `{rounded.lg}` | 16dp | Material 3 input fields, Primary CTA buttons (`54dp` height), OTP digit boxes, Highlighted Email Card, Role Context Pill, Hint Info Card |
| `{rounded.xl}` | 24dp | Login Hero Header Card, Student & Lecturer Role Selection Cards |
| `{rounded.pill}` | 9999dp | Segmented progress bars, step badges (`Bước 1/2`), role eyebrow pills, circular radio checkmarks |

### Iconography & Emblem Geometry
- **App Emblem in Hero Header:** Inside the `48dp × 48dp` (`{rounded.inner-icon}` `14dp`) container on the Login Hero card, display the **UniClass Interlocking U-C + QR Viewfinder** vector emblem (`ic_launcher_foreground`) over `{colors.rich-slate-blue}` (`#253745`) with a `1dp` `#669BA8AB` border.
- **Leading Input Icons:** `20dp × 20dp` stroke icons (`strokeWidth="2.0"`, rounded caps/joins) positioned `14dp` from the left edge of every Material 3 input box, tinted `{colors.muted-steel}` (`#4A5C6A`) at rest and `{colors.error-rose}` (`#F43F5E`) on error.

## Components

### Top Navigation & Progress

**`top-app-bar`** — Sticky header across all multi-step screens (`Register`, `VerifyOtp`, `ForgotPassword`, `ForgotPassNew`). Background `{colors.canvas-nordic-light}` at `90%` alpha, height `44dp` top row + `6dp` segmented bar row below.
- Left: `{component.button-icon-back}` (`40dp × 40dp` circular hit target, chevron-left icon in `{colors.obsidian-navy}`).
- Center: Step title (`"Tạo tài khoản mới"`, `"Thông tin tài khoản"`, `"Xác thực email"`, `"Khôi phục mật khẩu"`, `"Thiết lập mật khẩu mới"`) in `{typography.top-bar-title}` (`12sp / 700`, `{colors.midnight-slate}`).
- Right: `{component.step-badge-pill}` (`"Bước 1/2"`, `"Bước cuối"`, `"Bước 1/3"`, `"Bước 3/3"`) in `{colors.surface-frosted-80}` with `1dp` `{colors.pewter-mist}` border.
- Bottom Row: Horizontal row of 2 or 3 `{component.progress-segment-active}` (`#11212D`) and `{component.progress-segment-idle}` (`#CCD0CF`) bars (`6dp` height, `8dp` gap, `{rounded.pill}`).

### Buttons & Actions

**`button-primary-cta`** — Full-width primary action button pinned in the bottom thumb-zone. Height `54dp`, radius `{rounded.lg}` (`16dp`), background linear gradient (`#06141B → #11212D → #253745`), text `{colors.on-dark-silver}` (`#CCD0CF`) in `{typography.button-cta-primary}` (`14sp / 800`), trailing right-arrow icon (`16dp`).
- Active state: `{component.button-primary-cta-active}` — scales down to `0.98` (`transform: scale(0.98)`).

**`button-secondary-action`** — Compact dark slate button used inside `{component.register-prompt-card}` (`"Đăng ký →"`). Background `{colors.midnight-slate}` (`#11212D`), text `{colors.on-dark-silver}` (`#CCD0CF`), radius `{rounded.md}` (`12dp`), padding `8dp × 14dp`.

**`button-surface-action`** — Compact white utility button used inside tinted containers (`"Đổi vai trò"` in Role Context Pill, `"Sửa"` in Highlighted Email Card). Background `{colors.surface-pure-white}` (`#FFFFFF`), text `{colors.midnight-slate}` (`#11212D`), `1dp` solid `{colors.pewter-mist}` border, radius `8dp–12dp`.

### Cards & Containers

**`hero-header-card`** — Featured at the top of `LoginActivity`. Radius `{rounded.xl}` (`24dp`), padding `20dp`, background gradient `#06141B → #11212D → #253745`, border `1dp` `#804A5C6A`.
- Top Row: Left `48dp × 48dp` emblem box + `"UNICLASS • UIT"` pill + `"UniClass"` h1 (`20sp / 800` white); Right `{component.hero-qr-live-badge}` with pulsing `#CCD0CF` dot and `"QR Live"` label.
- Bottom Row (`16dp` top margin): `"Chào mừng bạn trở lại! 👋"` (`18sp / 800` white) + subtitle `"Điểm danh QR động, theo dõi lịch học & bài tập thời gian thực."` (`12sp / 400` `#9BA8AB`).

**`role-selection-card-selected` & `role-selection-card-idle`** — Tappable role cards on Registration Step 1 (`Sinh viên` & `Giảng viên`). Radius `{rounded.xl}` (`24dp`), padding `16dp`.
- Selected state: Background `{colors.surface-pure-white}` (`#FFFFFF`), `2dp` solid `{colors.midnight-slate}` (`#11212D`) border, `48dp × 48dp` icon container filled with `{colors.midnight-slate}` (Student) or `{colors.rich-slate-blue}` (Lecturer) and `#CCD0CF` icon, right `24dp` circular radio badge filled with `#11212D` and checkmark.
- Idle state: Background `{colors.surface-frosted-35}`, `2dp` solid `#999BA8AB` border, icon container in `#CCD0CF` with `#4A5C6A` icon, empty `24dp` radio ring in `#9BA8AB`.
- Footer Row: Separated by a `1dp` `{colors.frosted-silver}` hairline (`14dp` top margin, `12dp` top padding), containing 3 `{component.role-feature-chip}` items (`📷 Quét QR điểm danh`, `📚 Nộp bài & Tài liệu`, `📊 Xem bảng điểm` for Student; `📡 Phát QR Realtime`, `📝 Giao & Chấm bài`, `📈 Thống kê chuyên cần` for Lecturer).

**`role-context-pill`** — Displayed at the top of Registration Step 2. Radius `{rounded.lg}` (`16dp`), background `{colors.surface-frosted-55}`, `1dp` solid `{colors.pewter-mist}` border, padding `10dp 10dp 10dp 14dp`. Left: `10dp` `#11212D` dot + `"Vai trò:"` (`#4A5C6A`) + `"🎓 Sinh viên"` or `"👨‍🏫 Giảng viên"` (`12sp / 800` `#06141B`). Right: `{component.button-surface-action}` (`"Đổi vai trò"`).

**`highlighted-email-card`** — Displayed on OTP Verification screens. Radius `{rounded.lg}` (`16dp`), background `{colors.surface-frosted-65}`, `2dp` **dashed** `{colors.muted-steel}` (`#4A5C6A`) border, padding `14dp`.
- Left: `36dp × 36dp` rounded-xl (`12dp`) icon box in `#11212D` with `#CCD0CF` mail icon + `"EMAIL NHẬN MÃ"` eyebrow (`10sp / 700` `#4A5C6A`) + recipient email in `{typography.mono-email-value}` (`12sp / 700` `#06141B`).
- Right: `{component.button-surface-action}` with pencil icon + `"Sửa"` label to return and edit the email.

**`register-prompt-card`** — Bottom card on `LoginActivity` below the `"HOẶC"` divider. Radius `{rounded.lg}` (`16dp`), background `{colors.surface-frosted-45}`, `1dp` `#999BA8AB` border, padding `14dp`. Left: `"Chưa có tài khoản UniClass?"` (`12sp / 700` `#06141B`) + `"Dành cho Sinh viên & Giảng viên"` (`11sp` `#4A5C6A`). Right: `{component.button-secondary-action}` (`"Đăng ký →"`).

### Inputs, OTP & Live Validation

**`m3-input-field`** — Self-contained floating-label input box (`56dp` height, `{rounded.lg}` `16dp` radius).
- Default (`m3-input-field`): Background `#FFFFFF`, `1dp` `#CC9BA8AB` border, leading icon (`20dp`) in `#4A5C6A`, floating label in `#4A5C6A`, input text in `#06141B`.
- Focused (`m3-input-field-focused`): Border upgrades to `1.5dp` `#11212D` with `4dp` `#1A253745` focus ring; floating label transitions to `top: 9dp`, `11sp / 700` in `#11212D`.
- Error (`m3-input-field-error`): Border upgrades to `2dp` `{colors.error-rose}` (`#F43F5E`), field executes a `300ms` horizontal shake (`±5dp`), leading icon and label turn `#F43F5E`, and an inline error row (`11sp / 600` `#F43F5E` with exclamation icon) appears `6dp` below the box.

**`otp-digit-box-*`** — 6 individual numeric boxes (`48dp × 56dp` or flexible `0dp` weight with `6dp` gap, `{rounded.lg}` `16dp` radius, `{typography.otp-digit-display}`).
- Idle (`otp-digit-box-idle`): `#FFFFFF` background, `1dp` `#9BA8AB` border.
- Active/Focused (`otp-digit-box-active`): `#FFFFFF` background, `2dp` `#11212D` border, `4dp` `#26253745` outer ring.
- Filled (`otp-digit-box-filled`): `{colors.surface-frosted-45}` background, `2dp` `#253745` border, `#06141B` digit.
- Verified (`otp-digit-box-verified`): `#253745` background, `2dp` `#253745` border, `#CCD0CF` digit.

**`security-requirement-chip-*`** — Live password rule pills inside a white `{rounded.lg}` (`16dp`) card container (`1dp` `#999BA8AB` border, `14dp` padding).
- Idle (`security-requirement-chip-idle`): Background `{colors.surface-frosted-65}`, text `#4A5C6A`, prefix `"○"`.
- Active/Met (`security-requirement-chip-active`): Background `{colors.midnight-slate}` (`#11212D`), text `#CCD0CF`, prefix `"✓"`.

### Feedback Banners & Overlays

**`otp-auto-verify-banner`** — Sits `16dp` below the 6 OTP boxes (`54dp` min-height).
- Idle state: Left helper text `"Tự động kiểm tra khi nhập đủ 6 số"` (`12sp` `#4A5C6A`) + Right action `"Gửi lại (42s)"` (`12sp / 700` `#11212D`).
- Verifying state: Full-width `#11212D` rounded-2xl (`16dp`) banner with `1dp` `#4A5C6A` border, spinning `#CCD0CF` progress indicator, and `"Đang tự động xác thực mã OTP..."` (`12sp / 700` `#CCD0CF`).
- Verified state: Full-width `#253745` rounded-2xl (`16dp`) banner with `1dp` `#9BA8AB` border and `"✓ Xác thực hoàn tất! Đang chuyển trang..."`.

## Do's and Don'ts

### Do
- Use `{colors.canvas-nordic-light}` (`#F4F6F6`) as the root background of every authentication screen so `#FFFFFF` inputs and selected cards stand out cleanly.
- Anchor the top of `LoginActivity` with `{component.hero-header-card}` (`#06141B → #11212D → #253745`) and the bottom of every screen with `{component.button-primary-cta}`.
- Separate Registration into **Step 1 (Role Selection)** and **Step 2 (Account Form)** so users never scroll through role cards and 5 text fields simultaneously.
- Auto-trigger OTP verification the instant all 6 digits are populated (via typing or clipboard paste) and display `{component.otp-auto-verify-banner}`.
- Display `{component.highlighted-email-card}` with a `2dp` dashed `#4A5C6A` border and an inline **"Sửa"** button on OTP screens so users can immediately spot and fix a mistyped email.
- Use `JetBrains Mono` (`monospace`) for OTP digits and email readouts.

### Don't
- Don't use legacy bright blues (`#1976D2`, `#2563EB`) for buttons, headers, or steppers on authentication screens — every interactive dark surface must use the Nordic Slate tokens (`#06141B`, `#11212D`, `#253745`).
- Don't use bulky numbered circle steppers (`36dp` circles connected by horizontal lines) — always use `{component.top-app-bar}` with the `6dp` segmented progress bar.
- Don't leave users on intermediate "Success" screens (`RegisterSuccessActivity` or `PassChangedSuccessActivity`): Registration OTP auto-logs directly into Homepage, and Forgot Password Step 3 navigates directly to `LoginActivity`.
- Don't trigger a redundant `sendOtp` network call if the user taps **"Sửa"** on the OTP screen and returns without changing their email while the 60s cooldown is still active.
- Don't rely on Android's default popup `EditText.setError()` bubble — render inline rose-500 (`#F43F5E`) error text directly beneath the `16dp` rounded input field with a horizontal shake animation.

## Responsive Behavior

### Android Screen Breakpoints

| Name | Width / Height | Key Changes |
|---|---|---|
| Compact Phone | ≤ 360dp width or ≤ 680dp height | Hero card padding tightens from `20dp` to `16dp`; input vertical spacing tightens from `12dp` to `10dp`; ScrollView allows smooth overflow when IME opens |
| Standard Phone | 361dp–411dp width, 700dp–850dp height | Target baseline (`392dp × 790dp`); all 6 screens fit edge-to-edge without scrolling when keyboard is closed |
| Large Phone / Phablet | ≥ 412dp width | OTP boxes expand proportionally (`layout_weight="1"`, max width `54dp` each); content maintains `20dp` horizontal margins |
| Tablet / Landscape | ≥ 600dp width | Auth form container locks to `440dp` max width centered horizontally on the `#F4F6F6` canvas |

### Touch Targets
- Primary CTA (`{component.button-primary-cta}`): `54dp` height × full width.
- Material 3 Input Fields (`{component.m3-input-field}`): `56dp` height × full width.
- Password visibility toggle & Back button (`{component.button-icon-back}`): `40dp × 40dp` minimum touch area (`48dp` with padding).
- OTP Digit Boxes: `48dp × 56dp` each.

### Keyboard (IME) & Scroll Strategy
- Every screen applies `WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime()` padding to a `fillViewport="true"` `ScrollView` with `android:windowSoftInputMode="adjustResize"`.
- When the soft keyboard opens on Step 2 of Registration or Login, the focused `{component.m3-input-field}` scrolls smoothly into view while the bottom CTA remains accessible directly above the keyboard.

### Vector & Asset Behavior
- All icons (email, lock, eye, eye-off, user, phone, shield-check, key, graduation-cap, lecturer-board, edit-pencil, arrow-right, chevron-left) are crisp `24dp` Android `VectorDrawable` assets tinted via semantic color tokens.

## Iteration Guide

1. Reference component keys directly (`{component.hero-header-card}`, `{component.m3-input-field}`, `{component.highlighted-email-card}`) when building or reviewing XML layouts.
2. Variants of an existing component (`-selected`, `-idle`, `-focused`, `-error`, `-active`, `-verified`) map directly to Android `<selector>` state lists or view-binding state helpers.
3. Use `{colors.*}` token references mapped in `res/values/colors.xml` (`nordic_obsidian_navy`, `nordic_midnight_slate`, `nordic_rich_slate_blue`, `nordic_muted_steel`, `nordic_pewter_mist`, `nordic_frosted_silver`, `nordic_canvas_light`) — never hardcode raw hex strings in layouts.
4. Preserve the `16dp` (`{rounded.lg}`) vs `24dp` (`{rounded.xl}`) radius hierarchy: cards that frame a major section (Hero, Role Cards) use `24dp`; inputs, CTAs, OTP boxes, and utility cards use `16dp`.
5. Keep OTP verification automatic on the 6th digit (including clipboard paste) while retaining a fallback manual trigger or retry state if verification fails.
6. Ensure password criteria chips (`{component.security-requirement-chip-active}`) evaluate all required backend rules live as the user types.
7. Maintain strict Nordic Light mode across all Auth activities to transition seamlessly into the light-themed Student and Lecturer dashboards.

## Known Gaps

- **Simulated In-Screen Numeric Keypad vs System IME:** The HTML studio prototype renders a custom 3×4 numeric keypad at the bottom of the OTP screen for desktop browser demonstration. On real Android devices, the native numeric soft keyboard (`android:inputType="number"`) handles digit entry, so the custom HTML keypad is replaced by native IME focus and an optional **"Dán mã OTP"** (Paste OTP) quick action.
- **Simulated Push OTP Banner:** The floating Gmail banner in the HTML prototype is an interactive browser demo illustrating the TikTok-style leading-OTP email format (`"849201 là mã xác thực UniClass của bạn"`), which is already implemented in the Spring Boot backend email template (`EmailService.java`) and delivered by the Android OS notification shade.
- **Password Complexity Parity:** The HTML prototype displays 3 security chips (`Từ 8 ký tự`, `Chữ in hoa`, `Chữ số`), whereas `RegisterActivity` backend validation enforces 5 rules (`≥ 8 ký tự`, `Chữ in hoa`, `Chữ thường`, `Chữ số 0-9`, `Ký tự đặc biệt`). Production Android layouts render all 5 live chips so UI validation matches backend rules 100%.
