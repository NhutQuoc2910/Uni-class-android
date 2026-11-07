---
version: beta
name: UniClass-Unified-12-Color-Design-System
description: A unified 12-color architectural EdTech design system combining Nordic Slate & Deep Navy (#06141B, #11212D, #253745, #4A5C6A, #9BA8AB, #CCD0CF) with Modern Slate & Muted Lavender (#191D23, #57707A, #7B919C, #989DAA, #C5BAC4, #DEDCDC). Anchored by deep slate-navy Bento Hero cards, Digital Student ID cards, a pulsing Floating Center QR Attendance FAB, contextual Smart Top App Bars, and zero-friction authentication flows with Plus Jakarta Sans headlines and JetBrains Mono verification tokens.

colors:
  # Palette 1: Nordic Slate & Deep Navy
  obsidian-navy: "#06141B"
  midnight-slate: "#11212D"
  rich-slate-blue: "#253745"
  muted-steel: "#4A5C6A"
  pewter-mist: "#9BA8AB"
  frosted-silver: "#CCD0CF"

  # Palette 2: Modern Slate & Muted Lavender
  primary-ink: "#191D23"
  accent-teal: "#57707A"
  secondary-slate: "#7B919C"
  surface-slate: "#989DAA"
  muted-lavender: "#C5BAC4"
  warm-canvas: "#DEDCDC"

  # Light Surfaces & Tints
  canvas-nordic-light: "#F4F6F6"
  surface-pure-white: "#FFFFFF"
  surface-white-90: "#E6FFFFFF"
  surface-white-85: "#D9FFFFFF"
  surface-frosted-95: "#F2CCD0CF"
  surface-frosted-80: "#CCCCD0CF"
  surface-frosted-65: "#A6CCD0CF"
  surface-frosted-55: "#8CCCD0CF"
  surface-frosted-45: "#73CCD0CF"
  surface-frosted-35: "#59CCD0CF"
  surface-lavender-40: "#66C5BAC4"
  surface-lavender-20: "#33C5BAC4"

  # Semantic & State Tokens
  error-rose: "#F43F5E"
  error-rose-ring: "#33F43F5E"
  on-dark-white: "#FFFFFF"
  on-dark-silver: "#CCD0CF"
  on-dark-muted: "#9BA8AB"
  on-light-primary: "#191D23"
  on-light-obsidian: "#06141B"
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
  bento-hero-greeting:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: 20px
    fontWeight: 800
    lineHeight: 1.3
    letterSpacing: -0.3px
  hero-greeting:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: 18px
    fontWeight: 800
    lineHeight: 1.35
    letterSpacing: -0.25px
  digital-id-username:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: 18px
    fontWeight: 800
    lineHeight: 1.25
    letterSpacing: -0.2px
  bento-metric-value:
    fontFamily: "JetBrains Mono, monospace"
    fontSize: 18px
    fontWeight: 800
    lineHeight: 1.1
    letterSpacing: 0
  otp-digit-display:
    fontFamily: "JetBrains Mono, monospace"
    fontSize: 18px
    fontWeight: 800
    lineHeight: 1.0
    letterSpacing: 0
  role-card-title:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: 16px
    fontWeight: 800
    lineHeight: 1.3
    letterSpacing: -0.16px
  sheet-headline:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: 16px
    fontWeight: 800
    lineHeight: 1.3
    letterSpacing: -0.2px
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
  top-bar-title:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: 12px
    fontWeight: 700
    lineHeight: 1.2
    letterSpacing: -0.2px
  section-header-uppercase:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: 12px
    fontWeight: 800
    lineHeight: 1.2
    letterSpacing: 0.6px
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
  xs: 6px
  sm: 8px
  md: 12px
  inner-icon: 14px
  lg: 16px
  xl: 24px
  bento-hero: 26px
  digital-id: 28px
  sheet-top: 32px
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
  # 1. Contextual Smart Top App Bar
  smart-top-bar-home:
    backgroundColor: "{colors.warm-canvas}"
    textColor: "{colors.primary-ink}"
    height: 48px
    padding: 10px 20px
  smart-top-bar-profile:
    backgroundColor: "{colors.warm-canvas}"
    textColor: "{colors.primary-ink}"
    height: 48px
    padding: 10px 20px
  top-bar-role-pill:
    backgroundColor: "{colors.muted-lavender}"
    textColor: "{colors.primary-ink}"
    typography: "{typography.badge-pill}"
    rounded: "{rounded.pill}"
    padding: 4px 10px
  top-bar-edit-button:
    backgroundColor: "{colors.primary-ink}"
    textColor: "{colors.warm-canvas}"
    typography: "{typography.body-strong}"
    rounded: "{rounded.md}"
    padding: 6px 12px

  # 2. Dashboard Bento Hero & Cards
  bento-hero-card:
    backgroundColor: "{colors.midnight-slate}"
    textColor: "{colors.warm-canvas}"
    typography: "{typography.bento-hero-greeting}"
    rounded: "{rounded.bento-hero}"
    padding: 20px
  bento-metric-tile:
    backgroundColor: "{colors.primary-ink}"
    textColor: "{colors.on-dark-white}"
    typography: "{typography.bento-metric-value}"
    rounded: "{rounded.lg}"
    padding: 10px
  bento-metric-tile-highlight:
    backgroundColor: "{colors.primary-ink}"
    textColor: "{colors.muted-lavender}"
    typography: "{typography.bento-metric-value}"
    rounded: "{rounded.lg}"
    padding: 10px
  bento-live-class-banner:
    backgroundColor: "{colors.rich-slate-blue}"
    textColor: "{colors.on-dark-white}"
    typography: "{typography.body-strong}"
    rounded: "{rounded.lg}"
    padding: 12px
  bento-lavender-cta:
    backgroundColor: "{colors.muted-lavender}"
    textColor: "{colors.obsidian-navy}"
    typography: "{typography.badge-pill}"
    rounded: "{rounded.md}"
    padding: 6px 12px
  empty-onboarding-card:
    backgroundColor: "{colors.surface-frosted-65}"
    textColor: "{colors.primary-ink}"
    typography: "{typography.body-subtitle}"
    rounded: "{rounded.xl}"
    padding: 20px
  schedule-session-card:
    backgroundColor: "{colors.surface-white-90}"
    textColor: "{colors.primary-ink}"
    typography: "{typography.body-strong}"
    rounded: "{rounded.lg}"
    padding: 14px
  schedule-time-badge:
    backgroundColor: "{colors.primary-ink}"
    textColor: "{colors.muted-lavender}"
    typography: "{typography.mono-email-value}"
    rounded: "{rounded.md}"
    padding: 6px 8px

  # 3. Profile Digital Student ID & Settings
  digital-student-id-card:
    backgroundColor: "{colors.midnight-slate}"
    textColor: "{colors.warm-canvas}"
    typography: "{typography.digital-id-username}"
    rounded: "{rounded.digital-id}"
    padding: 20px
  digital-id-role-badge:
    backgroundColor: "{colors.muted-lavender}"
    textColor: "{colors.obsidian-navy}"
    typography: "{typography.micro-uppercase}"
    rounded: "{rounded.xs}"
    padding: 2px 8px
  digital-id-phone-cta:
    backgroundColor: "{colors.surface-lavender-20}"
    textColor: "{colors.muted-lavender}"
    typography: "{typography.badge-pill}"
    rounded: "{rounded.pill}"
    padding: 2px 10px
  settings-group-card:
    backgroundColor: "{colors.surface-white-90}"
    textColor: "{colors.primary-ink}"
    typography: "{typography.body-strong}"
    rounded: "{rounded.xl}"
    padding: 0px

  # 4. Bottom Navigation & Floating QR FAB
  bottom-nav-bar:
    backgroundColor: "{colors.surface-frosted-95}"
    textColor: "{colors.muted-steel}"
    height: 64px
    padding: 8px 12px 12px 12px
  bottom-nav-pill-active:
    backgroundColor: "{colors.primary-ink}"
    textColor: "{colors.muted-lavender}"
    rounded: "{rounded.pill}"
    padding: 4px 16px
  floating-qr-fab:
    backgroundColor: "{colors.rich-slate-blue}"
    textColor: "{colors.muted-lavender}"
    rounded: "{rounded.lg}"
    size: 56px 56px

  # 5. Interactive Bottom Sheets
  modal-bottom-sheet:
    backgroundColor: "{colors.warm-canvas}"
    textColor: "{colors.primary-ink}"
    typography: "{typography.sheet-headline}"
    rounded: "{rounded.sheet-top}"
    padding: 20px
  qr-scanner-bottom-sheet:
    backgroundColor: "{colors.obsidian-navy}"
    textColor: "{colors.warm-canvas}"
    typography: "{typography.sheet-headline}"
    rounded: "{rounded.sheet-top}"
    padding: 20px

  # 6. Authentication Components
  auth-top-app-bar:
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
  button-primary-cta:
    backgroundColor: "{colors.midnight-slate}"
    textColor: "{colors.on-dark-silver}"
    typography: "{typography.button-cta-primary}"
    rounded: "{rounded.lg}"
    height: 54px
  m3-input-field:
    backgroundColor: "{colors.surface-pure-white}"
    textColor: "{colors.obsidian-navy}"
    typography: "{typography.input-value}"
    rounded: "{rounded.lg}"
    height: 56px
    padding: 0px 16px
  highlighted-email-card:
    backgroundColor: "{colors.surface-frosted-65}"
    textColor: "{colors.obsidian-navy}"
    typography: "{typography.mono-email-value}"
    rounded: "{rounded.lg}"
    padding: 14px
  otp-digit-box-idle:
    backgroundColor: "{colors.surface-pure-white}"
    textColor: "{colors.obsidian-navy}"
    typography: "{typography.otp-digit-display}"
    rounded: "{rounded.lg}"
    size: 48px 56px
  otp-digit-box-verified:
    backgroundColor: "{colors.rich-slate-blue}"
    textColor: "{colors.on-dark-silver}"
    typography: "{typography.otp-digit-display}"
    rounded: "{rounded.lg}"
    size: 48px 56px
  security-requirement-row-active:
    backgroundColor: "{colors.midnight-slate}"
    textColor: "{colors.on-dark-silver}"
    typography: "{typography.badge-pill}"
    rounded: "{rounded.md}"
    padding: 5px 10px
  security-requirement-row-idle:
    backgroundColor: "{colors.surface-frosted-65}"
    textColor: "{colors.muted-steel}"
    typography: "{typography.badge-pill}"
    rounded: "{rounded.md}"
    padding: 5px 10px
  floating-snackbar:
    backgroundColor: "{colors.primary-ink}"
    textColor: "{colors.warm-canvas}"
    typography: "{typography.body-strong}"
    rounded: "{rounded.lg}"
    padding: 12px 16px
---

## Overview

UniClass's design system unifies **Authentication**, **Student/Lecturer Dashboards (Home)**, **Profile (Digital Student ID)**, and **Real-Time QR Attendance** under a cohesive **12-Color Architectural Palette**. It pairs **Palette 1 (Nordic Slate & Deep Navy)** with **Palette 2 (Modern Slate & Muted Lavender)** so that deep, authoritative academic cards (`#06141B → #11212D → #253745` and `#191D23`) contrast effortlessly against eye-soothing light canvases (`#DEDCDC` Warm Canvas on Main Tabs and `#F4F6F6` Nordic Light on Auth screens), highlighted by a tactile **Muted Lavender** (`#C5BAC4`) accent.

**Key Architectural Pillars:**
1. **12-Color Unified Theme:** Every surface, border, badge, and interactive control derives from 6 Nordic Slate tones and 6 Modern Slate & Lavender tones — eliminating harsh saturated blues and visual fragmentation between Auth and Main screens.
2. **Unified Bento Hero Dashboard:** Merges user greeting, 3-column live academic metrics (`Học phần`, `Điểm danh`, `Bài cần nộp`), and the `Lớp học hôm nay` / `LIVE` session banner into a single `26dp` Bento Hero card (`#06141B → #11212D → #253745`).
3. **Actionable Empty States:** Replaces repetitive gray placeholder strings (*"Hôm nay bạn không có ca học nào"*) with a structured onboarding card featuring a direct **`+ Tham gia lớp bằng mã`** Bottom Sheet trigger.
4. **Contextual Smart Top App Bar & Digital Student ID:** Eliminates the redundant 3x `"Sinh viên"` badge and duplicate Avatar/Contact cards on the Profile tab by switching the Top App Bar to a clean **`"Hồ sơ cá nhân"` + `"Chỉnh sửa"`** header and consolidating identity + contact info into a single `28dp` **Digital Student ID Card**.
5. **Floating Center QR Attendance FAB:** Elevates UniClass's core anti-cheat QR scanner into a `56dp × 56dp` rounded-2xl center button with a `#C5BAC4` border and subtle breathing pulse (`fab-qr-pulse`).
6. **Zero-Friction Nordic Light Auth:** Two-step registration (`Chọn vai trò` → `Thông tin tài khoản`), explicit vertical password requirement checklist (`Yêu cầu mật khẩu`), and hands-free 6-digit OTP auto-verification without redundant manual confirm buttons.

---

## Colors

> **Source studio analyzed:** [uniclass_auth_ui_redesign_studio.html](file:///home/nhutquoc2910/E/HK5/NT118/Project/Uni-class/Uni-class-android/theme/uniclass_auth_ui_redesign_studio.html) — covering Student Home (Empty & Active states), Student Profile (Digital Student ID), Floating QR Bottom Navigation, 4 Interactive Bottom Sheets (`qr_scanner`, `join_class`, `edit_profile`, `change_pass`), and the 6 Nordic Light Authentication screens.

### Palette 1 — Nordic Slate & Deep Navy (`nordic`)
1. **Obsidian Navy** (`{colors.obsidian-navy}` — `#06141B`): Deepest anchor tone. Used for Dark Mode screen canvas, start stop of the Bento Hero gradient, QR Scanner Bottom Sheet background, text on `#C5BAC4` lavender buttons/badges, and Auth headlines.
2. **Midnight Slate** (`{colors.midnight-slate}` — `#11212D`): Core dark surface tone. Used for the mid-stop of the Bento Hero & Digital Student ID cards, Dark Mode grouped cards, QR viewfinder background, and active password requirement rows.
3. **Rich Slate Blue** (`{colors.rich-slate-blue}` — `#253745`): Architectural slate tone. Powers the terminal stop of the Hero gradient, the integrated Live Class Banner inside the Bento Hero (`#253745` at `90%` alpha), afternoon schedule time badges (`13:30`), settings row icon containers, and verified OTP boxes.
4. **Muted Steel** (`{colors.muted-steel}` — `#4A5C6A`): Secondary structural tone. Used for Bento metric tile borders (`50%` alpha), Digital ID internal dividers, secondary body descriptions on light cards, and unfocused input icons.
5. **Pewter Mist** (`{colors.pewter-mist}` — `#9BA8AB`): Muted caption tone. Used for date eyebrows (`📅 Thứ Bảy...`), Bento metric labels (`Học phần`, `Bài cần nộp`), MSSV/Fullname subtitles on the Digital ID card, and resting input borders.
6. **Frosted Silver** (`{colors.frosted-silver}` — `#CCD0CF`): High-craft light neutral. Used for the Light Mode Bottom Navigation bar (`95%` alpha), empty-state onboarding card surface (`65%` alpha), secondary room badges (`Phòng A2.10`), and primary text on dark Auth CTAs.

### Palette 2 — Modern Slate & Muted Lavender (`lav`)
7. **Primary Ink** (`{colors.primary-ink}` — `#191D23`): Primary ink and high-contrast action surface in Light Mode. Used for main headings and body titles on `#DEDCDC`, primary dark CTA buttons (`Tham gia lớp bằng mã`, `Xác nhận tham gia lớp`, `Lưu thay đổi`), Bento metric tile backgrounds (`65%` alpha), morning schedule time badges (`07:30`), and active Bottom Nav pills in Light Mode.
8. **Accent Teal** (`{colors.accent-teal}` — `#57707A`): Structural accent & badge tone. Used for Bento Hero and Digital ID outer borders (`45%–60%` alpha), QR camera viewfinder border (`2dp`), `"Đã điểm danh"` status badges (`20%` alpha fill), and section action links (`Thời khóa biểu →`, `Xem tất cả →`).
9. **Secondary Slate** (`{colors.secondary-slate}` — `#7B919C`): Utility control tone. Used for settings row trailing chevrons (`>`), bottom sheet drag handles (`40dp × 6dp`), modal input borders, and secondary outline buttons.
10. **Surface Slate** (`{colors.surface-slate}` — `#989DAA`): Light mode hairline and card border token (`50%–60%` alpha on white/frosted cards and bottom nav top border) as well as inactive switch tracks.
11. **Muted Lavender** (`{colors.muted-lavender}` — `#C5BAC4`): Signature brand highlight across the 12-color system. Used for:
    - Top App Bar & Digital ID Role Badges (`🎓 Sinh viên`),
    - Highlighted attendance percentage in Bento Hero (`94%`),
    - Hero inline CTA buttons (`+ Nhập mã lớp`, `Quét QR →`),
    - Floating Center QR FAB border (`2dp`), icon, and pulsing glow,
    - Active Bottom Nav icon/text inside `#191D23` pills,
    - Digital ID avatar ring gradient (`#57707A → #C5BAC4`), camera edit button, and `+ Cập nhật SĐT ngay` pill,
    - Animated `qr-laser` beam and anti-cheat verification labels inside the QR Scanner sheet.
12. **Warm Canvas** (`{colors.warm-canvas}` — `#DEDCDC`): Soothing warm-gray Light Mode background canvas for Dashboard and Profile screens, eliminating pure-white glare while allowing `white/90` schedule and settings cards to stand out with crisp contrast.

---

## Typography

### Font Families
- **Primary UI & Display:** `Plus Jakarta Sans, sans-serif` — Geometric sans-serif used at weights `400`, `500`, `600`, `700`, and `800`.
- **Technical, Metrics & Verification Mono:** `JetBrains Mono, monospace` — Used for Bento Hero metric numbers (`0`, `94%`, `2`), class start times (`07:30`, `13:30`), join-class codes (`SE104X`), student email/phone values on the Digital ID card, and 6-digit OTP boxes.

### Hierarchy Table

| Token | Size | Weight | Line Height | Letter Spacing | Primary Usage |
|---|---|---|---|---|---|
| `{typography.bento-hero-greeting}` | 20px (20sp) | 800 | 1.30 | -0.3px | Greeting headline inside Bento Hero (`"Chào buổi tối, couq! 👋"`) |
| `{typography.screen-headline}` | 20px (20sp) | 800 | 1.25 | -0.4px | Primary h1 on Auth screens (`"Bạn tham gia với vai trò nào?"`, `"Nhập mã xác thực OTP"`) |
| `{typography.digital-id-username}` | 18px (18sp) | 800 | 1.25 | -0.2px | Student username inside Digital Student ID Card & Top Bar `"Hồ sơ cá nhân"` title |
| `{typography.bento-metric-value}` | 18px (18sp) | 800 | 1.10 | 0 | Monospace counters inside the 3 Bento Hero metric tiles (`4`, `94%`, `2`) |
| `{typography.otp-digit-display}` | 18px (18sp) | 800 | 1.00 | 0 | Monospace digits inside the 6 OTP boxes |
| `{typography.sheet-headline}` | 16px (16sp) | 800 | 1.30 | -0.2px | Bottom Sheet titles (`"Quét mã QR Điểm danh"`, `"Tham gia lớp học bằng mã"`) |
| `{typography.button-cta-primary}` | 14px (14sp) | 800 | 1.00 | 0 | Full-width primary CTA buttons |
| `{typography.input-value}` | 14px (14sp) | 600 | 1.40 | 0 | User-typed text inside inputs |
| `{typography.section-header-uppercase}` | 12px (12sp) | 800 | 1.20 | 0.6px | Section headers (`"LỊCH HỌC HÔM NAY (2 CA)"`, `"SẮP ĐẾN HẠN"`, `"TÀI KHOẢN & CÀI ĐẶT"`) |
| `{typography.body-strong}` | 12px (12sp) | 700 | 1.40 | 0 | Course titles (`"SE104.O21..."`), settings row titles, snackbar copy |
| `{typography.mono-email-value}` | 12px (12sp) | 700 | 1.30 | 0 | Email & phone values on Digital ID card, schedule time badges (`07:30`), OTP email box |
| `{typography.badge-pill}` | 11px (11sp) | 700 | 1.00 | 0.2px | Role pills (`🎓 Sinh viên`), password requirement rows, countdown badges (`Còn 5 giờ`) |
| `{typography.micro-uppercase}` | 10px (10sp) | 700 | 1.00 | 0.8px | Eyebrows (`"LỚP HỌC HÔM NAY"`, `"EMAIL NHẬN MÃ"`, `"EDULENS ANTI-CHEAT SCANNER"`) |

---

## Layout & Screen Architecture

### 1. Contextual Smart Top App Bar (`smartTopBar`)
 Solves the visual duplication problem between the global top bar and the Profile screen:
- **Default Mode (Home, Classes, Notifications):**
  - **Left:** `32dp × 32dp` (`rounded-xl` `12dp`) emblem container (`#191D23 → #253745` gradient, `1dp` `#57707A` border, `#C5BAC4` graduation cap icon) + `"UniClass"` brand title (`16sp / 800`).
  - **Right:** Role badge pill (`🎓 Sinh viên`, `bg-[#C5BAC4] text-[#191D23] border-[#989DAA]` in Light Mode) + `32dp` circular Avatar (`#253745 → #57707A` gradient, `2dp` `#C5BAC4` ring) which navigates to the Profile tab on tap.
- **Profile Mode (`activeTab === 'profile'`):**
  - **Left:** Screen title `"Hồ sơ cá nhân"` (`18sp / 800` `#191D23`).
  - **Right:** Compact action button `"✏️ Chỉnh sửa"` (`{component.top-bar-edit-button}`, `bg-[#191D23] text-[#DEDCDC]` in Light Mode) that opens the Edit Profile Bottom Sheet. The top-right avatar and role pill are hidden so `"Sinh viên"` never repeats 3 times on the Profile screen.

### 2. Student Home Screen (`buildHomeScreen`)
Structured vertically with `16dp` (`space-y-4`) rhythm inside a scrollable viewport (`16dp` horizontal padding, `112dp` bottom padding for the floating nav bar):
- **Unified Bento Hero Card (`{component.bento-hero-card}`):**
  - Radius `26dp`, padding `20dp`, gradient `135deg` `#06141B → #11212D → #253745`, border `1dp` `#57707A` (`45%` alpha), decorated with two soft blur orbs (`#C5BAC4/15` top-right, `#57707A/20` bottom-left).
  - **Header:** Date eyebrow (`📅 Thứ Bảy, 07/11/2026` in `#9BA8AB`), greeting `"Chào buổi tối, <username>! 👋"` (`20sp / 800` white), and contextual subtitle.
  - **3-Column Bento Metrics (`grid-cols-3 gap-2.5`):**
    1. `Học phần`: `#191D23/65` background, `1dp` `#4A5C6A/50` border, white monospace count.
    2. `Điểm danh`: `#191D23/65` background, `1dp` `#C5BAC4/45` border, `#C5BAC4` monospace percentage (`0%` or `94%`).
    3. `Bài cần nộp`: `#191D23/65` background, `1dp` `#4A5C6A/50` border, white monospace count.
  - **Integrated Live Class Banner (`{component.bento-live-class-banner}`):**
    - Sits `14dp` below the metrics inside the Hero card (`16dp` radius, `#253745/90` fill, `1dp` `#57707A/60` border, `12dp` padding).
    - *When `dataMode === 'empty'`:* Shows `+` icon box (`36dp`, `#11212D` fill, `#C5BAC4` icon) + `"LỚP HỌC HÔM NAY"` (`10sp` `#C5BAC4`) + `"Hôm nay chưa có ca học nào"` + button `"+ Nhập mã lớp"` (`{component.bento-lavender-cta}`).
    - *When `dataMode === 'active'`:* Shows `"LIVE"` badge (`36dp`, `#C5BAC4` fill, `#06141B` bold text) + `"Đang mở điểm danh • Phòng B3.04"` + course name + button `"Quét QR →"`.
- **Empty State Onboarding (`dataMode === 'empty'`):**
  - Replaces redundant gray empty-text blocks with `{component.empty-onboarding-card}` (`24dp` radius, `#CCD0CF/65` fill, `1dp` `#989DAA/60` border, `20dp` padding): `56dp` rounded-2xl icon box (`#191D23` fill, `#C5BAC4` stack icon), title `"Bạn chưa tham gia lớp học phần nào"`, subtitle guiding the student to enter a 6-character class code, and primary CTA `"➕ Tham gia lớp bằng mã"` (`bg-[#191D23] text-[#DEDCDC]`).
  - Below sits the compact `"SẮP ĐẾN HẠN"` section (`0 bài tập`) with a reassuring check card (`"Tuyệt vời! Không có bài tập tồn đọng"`).
- **Active State Schedule & Deadlines (`dataMode === 'active'`):**
  - **Lịch học hôm nay:** Section header with `"Thời khóa biểu →"` action link. Each class uses `{component.schedule-session-card}` (`16dp` radius, `white/90` fill, `1dp` `#989DAA/50` border): left `48dp` `{component.schedule-time-badge}` (`07:30` / `Tiết 1-3`), middle room badge (`Phòng B3.04` in `#C5BAC4/35`) + lecturer name + bold course title, right status pill (`Đã điểm danh` in `#57707A/20` or `Quét QR` button in `bg-[#191D23] text-[#C5BAC4]`).
  - **Sắp đến hạn:** Assignment card with `40dp` `#C5BAC4/40` document icon box, assignment title, course + deadline subtitle, and right countdown pill (`Còn 5 giờ`, `bg-[#191D23] text-[#C5BAC4]`).

### 3. Student Profile Screen (`buildProfileScreen`)
- **Digital Student ID Card (`{component.digital-student-id-card}`):**
  - Consolidates user identity and contact details into a single `28dp` card (`#191D23 → #11212D → #253745` gradient, `1dp` `#57707A/60` border, `20dp` padding) with a subtle watermark seal (`144dp` circle, `14dp` `#C5BAC4/10` border at bottom-right).
  - **Identity Row:** `64dp × 64dp` (`rounded-2xl`) avatar with `#57707A → #C5BAC4` gradient frame, `#11212D` inner fill, `24sp / 800` `#C5BAC4` initial, and a `24dp` circular camera edit button (`bg-[#C5BAC4] text-[#06141B]`) anchored at `-bottom-1 -right-1`. Next to it: username (`18sp / 800` white), a single `{component.digital-id-role-badge}` (`Sinh viên`), and `"<FullName> • MSSV: <studentId>"` (`12sp` `#9BA8AB`).
  - **Integrated Contact Footer:** Separated by a `1dp` `#4A5C6A/50` top hairline (`16dp` margin-top, `14dp` padding-top). Displays `Email:` (with `#C5BAC4` mail icon + white monospace address) and `Điện thoại:` (with `#C5BAC4` phone icon + either monospace phone number or `{component.digital-id-phone-cta}` `"+ Cập nhật SĐT ngay"`).
- **Account & Settings Group (`{component.settings-group-card}`):**
  - `24dp` radius container (`white/90` in Light Mode, `#11212D` in Dark Mode, `1dp` `#989DAA/50` border) housing 3 interactive rows separated by `1dp` `#DEDCDC` hairlines:
    1. `Chỉnh sửa hồ sơ` (`36dp` `#253745` icon box with `#C5BAC4` pencil icon).
    2. `Đổi mật khẩu` (`36dp` `#57707A/25` icon box with `#57707A` lock icon).
    3. `Giao diện Midnight Dark` (`36dp` `#C5BAC4/35` icon box with moon icon + `44dp × 24dp` toggle switch).
- **Logout Action:** Standalone `16dp` rounded button (`bg-white/80 border-rose-300 text-rose-700` in Light Mode; `bg-rose-950/30 border-rose-800/50 text-rose-300` in Dark Mode).

### 4. Bottom Navigation & Floating Center QR FAB (`renderBottomNav`)
- **Primary Pattern — Floating Center QR Button (`floatingQrNav = true`):**
  - 5-column grid inside a frosted bar (`#CCD0CF/95` with `1dp` `#989DAA/60` top border in Light Mode; `#11212D/95` with `#253745` top border in Dark Mode).
  - **Columns 1, 2, 4, 5:** `Trang chủ`, `Lớp học`, `Thông báo`, `Cá nhân`. Active tab renders a `32dp` height pill (`{component.bottom-nav-pill-active}`: `bg-[#191D23] text-[#C5BAC4]` in Light Mode, `bg-[#C5BAC4] text-[#06141B]` in Dark Mode) and `10sp / 800` label.
  - **Column 3 (Center Thumb-Zone):** Elevated `56dp × 56dp` (`rounded-2xl` `16dp`) `{component.floating-qr-fab}` positioned `-4dp` above the bar (`#191D23 → #253745 → #57707A` gradient, `2dp` `#C5BAC4` border, `28dp` `#C5BAC4` QR icon, `fab-qr-pulse` shadow animation) with `"Quét QR"` (`10sp / 800`) label anchored below.

### 5. Interactive Bottom Sheets (`openSheet`)
All modal sheets slide up over a `#06141B/75` blurred backdrop with `32dp` top corners (`{rounded.sheet-top}`) and a centered `40dp × 6dp` drag handle:
- **QR Attendance Scanner Sheet (`qr_scanner`):** Dark `#06141B` surface with `1dp` `#57707A` top border, `"EDULENS ANTI-CHEAT SCANNER"` eyebrow (`#C5BAC4`), `208dp` viewfinder (`#11212D` fill, `2dp` `#57707A` border, `144dp` dashed `#C5BAC4/70` reticle, and `2.4s` vertical `qr-laser` animation), 3 hardware verification badges (`📍 GPS Hợp lệ`, `📶 WiFi Trường`, `🛡️ Cảm biến`), and a full-width `#C5BAC4` action button.
- **Join Class Sheet (`join_class`):** `48dp` height uppercase monospace class-code input (`border-2 border-[#57707A]`) + `"Xác nhận tham gia lớp"` CTA (`bg-[#191D23] text-[#C5BAC4]`).
- **Edit Profile Sheet (`edit_profile`):** Display name and monospace phone inputs + `"Lưu thay đổi"` CTA (`bg-[#191D23] text-[#C5BAC4]`).
- **Change Password Sheet (`change_pass`):** Current and new password inputs + `"Cập nhật mật khẩu"` CTA (`bg-[#191D23] text-[#C5BAC4]`).

### 6. Authentication Screens (Nordic Light Refinements)
- **Login (`LoginActivity`):** Hero Header Card uses the clean brand badge **`"UniClass"`** (no `"UIT"` suffix) alongside the `"QR Live"` status pill.
- **Register Step 1 (`RegisterActivity` — Role Selection):** Displays the headline **`"Bạn tham gia với vai trò nào?"`** directly below the Top App Bar (the redundant `"🎓 Cá nhân hóa trải nghiệm"` eyebrow badge is removed). Role feature chips use clean text (`Quét QR điểm danh`, `Nộp bài & Tài liệu`, `Xem bảng điểm`, `Phát QR Realtime`, `Giao & Chấm bài`, `Thống kê chuyên cần`).
- **Register Step 2 & Reset Password (`Yêu cầu mật khẩu`):** The password complexity module is titled **`"Yêu cầu mật khẩu"`** and renders a **vertical list of 5 explicit full-width requirement rows** (`{component.security-requirement-row-idle}` → `{component.security-requirement-row-active}`):
  1. `○ / ✓ Tối thiểu 8 ký tự`
  2. `○ / ✓ Có ít nhất 1 chữ in hoa (A-Z)`
  3. `○ / ✓ Có ít nhất 1 chữ thường (a-z)`
  4. `○ / ✓ Có ít nhất 1 chữ số (0-9)`
  5. `○ / ✓ Có ít nhất 1 ký tự đặc biệt (!@#$...)`
- **OTP Verification (`VerifyOtpActivity` & `ForgotPassOTPActivity`):** Pure hands-free auto-verification. As soon as all 6 digits are entered or pasted via `"⚡ Dán mã OTP từ bộ nhớ tạm"`, verification triggers automatically. The redundant helper text (*"Tự động kiểm tra khi nhập đủ 6 số"*) and manual **`"Xác nhận OTP →"`** button are removed, leaving the countdown timer (`"Gửi lại (60s)"`) cleanly centered below the 6 OTP boxes.

---

## Do's and Don'ts

### Do
- Use the **12-Color Unified Palette** consistently: `#DEDCDC` (`warm-canvas`) or `#F4F6F6` (`canvas-nordic-light`) for light backgrounds, `#06141B → #11212D → #253745` for Hero/ID cards, `#191D23` for primary light-mode CTAs/pills, and `#C5BAC4` (`muted-lavender`) for high-craft accents and badges.
- Switch the Top App Bar contextually when entering the **Profile** tab: hide the top-right Avatar and `"Sinh viên"` pill and display `"Hồ sơ cá nhân"` + `"Chỉnh sửa"`.
- Merge the Home greeting, 3 summary counters (`Học phần`, `Điểm danh`, `Bài cần nộp`), and today's live session status into a single **Bento Hero Card**.
- Use a **vertical 5-row list** for `"Yêu cầu mật khẩu"` with explicit Vietnamese descriptions so rules never wrap awkwardly or truncate on compact screens.
- Keep OTP screens free of manual submit buttons — let the 6th digit trigger verification automatically with acentered resend timer.

### Don't
- Don't include `"UIT"` or `@uit.edu.vn` in any UI badges, placeholders, or calendar UIDs — UniClass is a standalone product (`"UniClass"`, `@uniclass.app`).
- Don't repeat the `"Sinh viên"` / `"Giảng viên"` role badge 3 times on the Profile screen or render a separate white Contact Card below the Avatar header; integrate Email and Phone directly inside the **Digital Student ID Card**.
- Don't show 3 separate gray empty-state text boxes on the Home screen when a new user has 0 classes — render the actionable **Empty Onboarding Card** with `+ Tham gia lớp bằng mã`.
- Don't display the `"🎓 Cá nhân hóa trải nghiệm"` eyebrow badge above `"Bạn tham gia với vai trò nào?"` on Register Step 1.
- Don't display `"Tự động kiểm tra khi nhập đủ 6 số"` or a manual `"Xác nhận OTP →"` button on OTP verification screens.
