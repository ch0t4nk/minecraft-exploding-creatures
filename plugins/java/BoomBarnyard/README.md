# BoomBarnyard Plugin

Explodes configured passive mobs (pigs, chickens, cows, sheep) when they die.

## Key Settings
- `explosion-power`: Base power (creeper ≈3, TNT = 4)
- `max-explosion-power`: Safety clamp.
- `per-entity-powers`: Override base power per entity (clamped)
- `enabled-entities`: Now supports PIG, CHICKEN, COW, SHEEP by default
- `nuclear.chance`: Probability newly spawned enabled entity becomes nuclear (default raised to 0.08 in your runtime config for testing; source default 0.08)
- `nuclear.*`: Controls rare mega explosion effects (health reduction, launch, glow, countdown)

## Commands
- `/boombarnyard reload` – Reload config
- `/boombarnyard test <ENTITY>` – Spawn and kill entity for testing

## Safety Notes
High nuclear power or high chance can cause lag; lower after testing (e.g. set `nuclear.chance` back to 0.02 or less).

## Self-Test
Optional automatic test spawn via `self-test` config or `run-self-test` trigger file.

## Ideas to Extend
- Add more animals.
- Different explosion sizes per animal.
- Particle effects or sounds before boom.

Have fun experimenting!
