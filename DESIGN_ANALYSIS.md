# Beekeeper App - Design Analysis

**Date:** November 6, 2025

## Design System Overview

### Color Palette
- **Primary Background:** Dark green (#1a2e1a, #0d1f0d) - Deep forest green
- **Secondary Background:** Dark gray-green (#2a3a2a)
- **Accent/Primary:** Gold/Yellow (#FFC107, #FFB300) - For CTAs, highlights
- **Secondary Accent:** Bright green (#4CAF50, #00E676) - For positive status
- **Warning:** Orange (#FF9800, #FFA726)
- **Error/Alert:** Red (#F44336, #E53935)
- **Success:** Green checkmark color (#4CAF50)
- **Text:** White (#FFFFFF) for primary text, light gray for secondary

### Typography
- **Headers:** Bold, white text
- **Body:** Regular weight, white/light gray
- **Status indicators:** Small, colored text with icons

### UI Components
- **Cards:** Dark green background with rounded corners
- **FAB (Floating Action Button):** Gold/yellow circular button with + icon
- **Status badges:** Small colored circles or icons (green check, yellow warning, red error)
- **Bottom Navigation:** Dark background with icons and labels, highlighted tab in gold

---

## Screen Breakdown

### Screen 1: Apiary Sites Overview
**Title:** "My Apiaries"

**Components:**
- **Header bar:** Dark green with hamburger menu (left) and filter icon (right)
- **Apiary cards:** Each card shows:
  - Apiary name (white, bold)
  - Location/address (gray text)
  - Number of hives (with hive icon)
  - Status indicator (right side): green checkmark, yellow warning triangle, or red error circle
  - Right arrow for navigation
- **FAB:** Gold circular button with + icon (bottom right)

**Data shown:**
- Backyard Garden - Sunnydale, CA - 5 Hives (green check)
- Hillside Meadow - 45.123, -122.456 - 8 Hives (yellow warning)
- Riverbend Apiary - Cloverdale, OR - 3 Hives (red error)

---

### Screen 2: Site Dashboard - Hillside Farm
**Title:** "Hillside Farm"

**Components:**
- **Header:** Back button (left), title, settings icon (right)
- **Alert banner:** Yellow background with warning icon
  - "Swarm Warning"
  - "AI Alert: High Swarm Probability in this area. Check hives A-01 and C-04."
  - "Dismiss" button (gold)
- **Hive grid:** 2 columns of hive cards
  - Hive photo (large image showing actual hive/bees)
  - Hive ID (e.g., "Hive A-01")
  - Status tag (green "Strong", orange "Needs inspection", yellow "Alert")
  - Last inspection time ("Inspected: 3 days ago")
- **Bottom navigation:** Dashboard (gold, active), Tasks, Map, Profile

**Hives shown:**
- Hive A-01: Photo of bees on comb, "Strong", inspected 3 days ago
- Hive A-02: Photo of bee box, "Needs inspection", inspected 14 days ago
- Hive B-01: Photo of bees on frame, "Strong", inspected 5 days ago
- Hive B-02: Photo of yellow box in field, "Alert", inspected 2 days ago

---

### Screen 3: Hive Details - Hive 04
**Title:** "Hive 04" with subtitle "Meadow Field"

**Components:**
- **Header:** Back button, title with subtitle, menu icon
- **Tabs:** Overview (active, gold underline), Logbook, Photos
- **Current Status section:**
  - 2x2 grid of status cards:
    - "Colony Strength: Strong" (green dot)
    - "Queen Status: Laying" (green dot)
    - "Temperament: Calm" (orange dot)
    - "Honey Stores: Full" (green dot)
- **AI Recommendations section:**
  - Card 1 (green-tinted background):
    - Checkmark icon
    - "Hive is Thriving"
    - "Excellent honey stores and strong population. Consider adding a super soon to prevent swarming."
  - Card 2 (dark background):
    - Warning icon (yellow)
    - "Monitor Varroa Mites"
    - "Your last mite count was 3 weeks ago. It's recommended to perform a new count within the next 7 days."
- **FAB:** Gold + button

---

### Screen 4: AI Expert Advisor
**Title:** "AI Expert Advisor"

**Components:**
- **Header:** Hamburger menu, title, notification bell icon
- **Weather widget:**
  - "Weather for: Sunny Meadow Apiary"
  - "Good conditions for hive work."
  - "The bees will be calm and active today."
  - Weather icon (sun with cloud)
  - Stats: 72°F, 10% (humidity), 5 mph (wind)
- **Today's AI Advice section:**
  - Card 1 (checkmark icon, green):
    - "Ideal day for a full hive inspection."
    - "Weather conditions are perfect. Check for queen health, brood pattern, and food stores."
  - Card 2 (hexagon icon, green):
    - "High nectar flow expected."
    - "Check for super space to avoid swarming. Add a new super to hive 01 and 03."
- **Upcoming Alerts & Reminders:**
  - Calendar icon (gold): "Swarm Season Begins - In 2 weeks"
  - Warning icon (orange): "Mite treatment due - For Hive 02"
  - Water/humidity icon (blue): "High humidity overnight - Ensure hive ventilation"
- **Bottom navigation:** Dashboard, Hives, Advisor (gold, active), Tasks, Profile

---

### Screen 5: Task Scheduler
**Title:** "My Tasks"

**Components:**
- **Header:** Hamburger menu, title, filter icon
- **Toggle:** List (active) / Calendar
- **Task list:** Each task has:
  - Colored left border (red for overdue, orange for today, blue for upcoming, green for completed)
  - Task icon
  - Task title
  - Hive/location and due date
  - Tasks shown:
    - "Inspect Hive A-1 for Mites" - Hive A-1 | Due: Yesterday (red border, magnifying glass icon)
    - "Harvest Honey from Site B" - Site B | Due: Today (orange border, honey icon)
    - "Start Varroa Mite Treatment" - Hive C-3 | Due: In 3 days (blue border, treatment icon)
    - "Feed Hive A-1 Sugar Water" - Hive A-1 | Due: In 5 days (blue border, feeding icon)
    - "Check Queen C-3 Status" - Hive C-3 | Completed (green border, checkmark, grayed out)
- **FAB:** Green + button

---

### Screen 6: User Profile Page
**Title:** "Profile"

**Components:**
- **Header:** Back button, title
- **Profile section:**
  - Avatar image (illustrated character with beekeeping hat)
  - Name: "Alexi Beekeeper"
  - Email: "alexi.beekeeper@gmail.com"
- **Settings menu items:**
  - "Account Settings" - Icon, title, subtitle, right arrow
    - "Manage your personal information and password"
  - "App Preferences"
    - "Notifications, units, and appearance"
  - "Subscription"
    - "View your current plan and billing"
  - "Help & Support"
    - "Find answers and get in touch"
- **Logout button:** Red button with logout icon at bottom

---

### Screen 7: Managed Apiaries (Table View)
**Title:** "Managed Apiaries"

**Components:**
- **Header:** Hamburger menu, title, filter icon
- **Table view:**
  - Columns: SITE NAME, LAST INSP., HEALTH %, TASKS
  - Rows with data:
    - Backyard... (Sunnydale, CA) - 2023-10-26 - 92% (blue bar) - 2 tasks
    - Hillside... (45.123, -122...) - 2023-11-15 - 68% (yellow bar) - 5 tasks
    - Riverbend... (Cloverdale, OR) - 2023-09-01 (red date) - 35% (red bar) - 12 tasks
  - Right arrow for each row
- **FAB:** Blue + button
- **Dark blue/gray background**

---

### Screen 8: Geospatial Overview
**Title:** "Geospatial Overview"

**Components:**
- **Header:** Hamburger menu, title, layers/settings icon
- **Map view:**
  - Dark themed map
  - Location markers for each apiary:
    - Apiary A (blue marker, top)
    - Apiary B (orange marker, middle)
    - Apiary C (red/pink marker, bottom)
- **Bottom sheet:** "Apiary Details" (collapsible panel)
- **FAB:** Blue + button

---

## Design Patterns & Interactions

### Navigation
- **Bottom Navigation Bar:** 5 items (Dashboard, Hives/Tasks, Advisor/Map, Tasks/Profile)
- **Hamburger Menu:** For additional navigation/settings
- **Back Buttons:** Standard Android/iOS back navigation
- **Right Arrows:** Navigate to detail screens

### Status Indicators
- **Green:** Healthy, good, strong
- **Yellow/Orange:** Warning, needs attention, alert
- **Red:** Error, critical, overdue

### Cards
- **Rounded corners:** ~12-16dp radius
- **Elevated/shadow:** Subtle shadow for depth
- **Dark background:** Consistent dark green theme
- **Clear typography hierarchy:** Bold titles, regular body text

### Icons
- **Status icons:** Checkmark, warning triangle, error circle
- **Action icons:** +, settings gear, filter, menu hamburger, back arrow
- **Contextual icons:** Hive, weather, calendar, magnifying glass, etc.

### Photos
- Real hive/bee photos prominently displayed in cards
- Photos are key to visual identification

---

## Implementation Priority

### Phase 1: Core Navigation & Structure
1. Bottom navigation bar
2. Main app theme (colors, typography)
3. Basic screen structure for each view

### Phase 2: Primary Screens
1. Apiary List (Screen 1)
2. Site Dashboard (Screen 2)
3. Hive Detail (Screen 3)

### Phase 3: Smart Features
1. AI Expert Advisor (Screen 4)
2. Task Scheduler (Screen 5)

### Phase 4: Supporting Screens
1. User Profile (Screen 6)
2. Table View (Screen 7)
3. Map View (Screen 8)

### Phase 5: Advanced Features
1. Photo capture and display
2. AI image analysis integration
3. Weather API integration
4. Notifications

---

## Technical Notes

- **Dark Theme:** App is primarily dark-themed
- **Material Design 3:** Follows modern Material Design principles
- **Responsive:** Should work on phones and tablets
- **Compose Multiplatform:** Use Jetpack Compose for all UI
- **Navigation:** Implement NavHost with bottom bar navigation
- **State Management:** ViewModels for each screen
- **API Integration:** Backend already created, needs to be connected

---

## Design Assets Needed

- Bee/hive icons
- Weather icons
- Status indicator icons
- Placeholder hive photos
- User avatar illustrations
- Map markers

