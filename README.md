# Infinite Cave Explorer 🚀

A fast-paced arcade survival game built completely from scratch in **Java AWT**. Pilot a custom spaceship through an endless, procedurally generated cavern. Survive as long as you can while the game dynamically scales in difficulty—increasing speed and narrowing the cave walls the further you go!

![Gameplay Screenshot](placeholder-for-screenshot.gif) 
*(Note: Replace `placeholder-for-screenshot.gif` with a real screenshot or GIF of your game in action!)*

## 🎮 Controls

| Action | Keybinding |
| :--- | :--- |
| **Move Up** | `Up Arrow` or `W` |
| **Move Down** | `Down Arrow` or `S` |
| **Pause/Resume** | `Space Bar` |
| **Restart (Game Over)** | `R` |

---

## ⚙️ Under the Hood: Step-by-Step Architecture

This project was built without external game engines (like Unity or Godot) to demonstrate a deep understanding of core Computer Science and Object-Oriented principles. Here is a step-by-step breakdown of how the game actually works:

### Step 1: The Custom Game Loop (Multithreading)
Native Java GUI applications are event-driven, which isn't suitable for real-time games. To fix this, the game runs on a custom engine:
1. **The Thread:** The main `GameCanvas` implements `Runnable` and runs on its own dedicated `Thread`.
2. **The Loop:** An infinite `while(running)` loop acts as the heartbeat of the game. 
3. **Tick & Render:** Every cycle, the loop calculates the elapsed time. It updates the game's logic (moving the player, shifting the cave) and then forces the screen to repaint, targeting a smooth 60 Frames Per Second (FPS).

### Step 2: Procedural World Generation
The cave isn't a pre-drawn image; it is mathematically generated in real-time.
1. **Segment Slicing:** The cave is made of a `List` of thin, vertical segments.
2. **Scrolling Illusion:** In every frame, all existing segments are moved a few pixels to the left. The player's ship never actually moves forward; the world moves backward around it.
3. **Generate & Discard:** As soon as a segment moves past the left edge of the screen, it is deleted from memory. Simultaneously, a new segment is generated just off the right edge. 
4. **Smoothed Random Walk:** The ceiling and floor heights of the new segments are generated using a constrained random algorithm, ensuring the cave path twists unpredictably but remains playable.

### Step 3: Dynamic Difficulty Scaling
The game mathematically adapts to the player's survival time.
1. **Speed Multiplier:** As the score increases, the horizontal shift of the cave segments gets larger, increasing the scroll speed.
2. **Narrowing Passages:** The vertical distance between the newly generated ceiling and floor slowly decreases based on the current score. The further you fly, the less room for error you have.

### Step 4: Anti-Flicker Graphics (Double Buffering)
Because Java AWT draws graphics directly to the screen by default, clearing and redrawing the screen 60 times a second causes severe visual flickering. 
1. **Off-Screen Canvas:** The game creates a hidden `Image` in memory that perfectly matches the screen size.
2. **Background Painting:** During the render step, the background, the cave polygons, the UI text, and the player are all drawn onto this hidden memory canvas first.
3. **The Swap:** Once the entire frame is completely drawn off-screen, the graphics engine draws that single, completed image onto the visible monitor. This eliminates flickering entirely.

### Step 5: State Management & Collision
1. **Hitboxes:** The game constantly checks the mathematical bounds of the player's custom polygon against the coordinates of the cave segments currently occupying the same X-axis space.
2. **State Machine:** If a collision is detected, the game loop instantly switches a boolean state from `PLAYING` to `GAME_OVER`, stopping the world generation and displaying the restart UI.

---

## 💻 Tech Stack & OOP Concepts

* **Language:** Java 
* **GUI Toolkit:** Java AWT (`java.awt.*` and `java.awt.event.*`)
* **Encapsulation:** Distinct classes manage their own data (`Player` manages its coordinates, `WorldManager` manages the segment list).
* **Inheritance & Abstraction:** Utilizes AWT's `Canvas` component and overrides adapter classes (`KeyAdapter`, `WindowAdapter`) for clean event handling.

## 🚀 How to Run Locally

Ensure you have the [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/downloads/) installed on your machine.

1. Clone the repository:
   ```bash
   git clone https://github.com/SrivathsavaRebba/Infinite_Cave_Explorer_Game.git
