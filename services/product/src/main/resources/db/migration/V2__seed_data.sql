-- Seed data for hardware and tools catalog
-- PostgreSQL compatible

BEGIN;

-- Optional cleanup
DELETE FROM product;
DELETE FROM category;

INSERT INTO category (id, description, name) VALUES
    (1, 'Hand tools for manual work, fastening, cutting, and assembly.', 'Hand Tools'),
    (2, 'Power-driven tools for drilling, cutting, grinding, and demolition.', 'Power Tools'),
    (3, 'Fasteners and mounting hardware for construction and repairs.', 'Fasteners'),
    (4, 'Electrical tools, testers, and accessories for wiring and diagnostics.', 'Electrical'),
    (5, 'Workshop storage, organization, and transport solutions.', 'Storage & Organization'),
    (6, 'Measuring, marking, and inspection instruments.', 'Measuring Tools'),
    (7, 'Safety gear and protective equipment for workshop and site use.', 'Safety Equipment'),
    (8, 'Gardening and outdoor maintenance tools and accessories.', 'Outdoor Tools');

INSERT INTO product (id, description, name, available_quantity, price, category_id) VALUES
    (1, '16 oz claw hammer with fiberglass handle for general carpentry.', 'Claw Hammer', 42, 18.99, 1),
    (2, 'Professional screwdriver set with Phillips, flat, and Torx bits.', 'Precision Screwdriver Set', 65, 24.50, 1),
    (3, 'Adjustable wrench with chrome finish and smooth jaw action.', 'Adjustable Wrench 10in', 34, 16.75, 1),
    (4, 'Slip-joint pliers for gripping, turning, and cutting light wire.', 'Slip-Joint Pliers', 51, 12.40, 1),
    (5, 'Needle nose pliers for precision gripping in tight spaces.', 'Needle Nose Pliers', 47, 13.10, 1),
    (6, 'Heavy-duty utility knife with retractable blade and spare blades.', 'Utility Knife', 88, 9.99, 1),
    (7, 'Metric hex key set in folding holder for bicycle and machinery work.', 'Hex Key Set Metric', 39, 14.20, 1),
    (8, '24 oz rip hammer designed for demolition and framing.', 'Rip Hammer', 21, 22.95, 1),
    (9, 'Soft-faced rubber mallet for flooring, tiles, and delicate assembly.', 'Rubber Mallet', 29, 11.85, 1),
    (10, 'Compact mini hacksaw for cutting metal, PVC, and rods.', 'Mini Hacksaw', 26, 10.60, 1),
    (11, '18V cordless drill driver with battery and charger included.', 'Cordless Drill 18V', 31, 129.99, 2),
    (12, 'Hammer drill with variable speed and masonry capability.', 'Hammer Drill 900W', 17, 149.00, 2),
    (13, 'Angle grinder with 125 mm disc support and side handle.', 'Angle Grinder 125mm', 23, 89.50, 2),
    (14, 'Orbital sander for smooth finishing on wood and painted surfaces.', 'Orbital Sander', 19, 74.90, 2),
    (15, 'Circular saw for wood panels and framing cuts.', 'Circular Saw 185mm', 14, 139.95, 2),
    (16, 'Jigsaw with variable speed and quick blade change system.', 'Jigsaw 650W', 18, 92.30, 2),
    (17, 'Oscillating multi-tool for cutting, scraping, and sanding.', 'Oscillating Multi-Tool', 16, 99.00, 2),
    (18, 'Heat gun with dual temperature settings for shrink wrap and paint removal.', 'Heat Gun 2000W', 24, 39.95, 2),
    (19, 'Rotary tool kit with accessories for detail work and engraving.', 'Rotary Tool Kit', 27, 58.80, 2),
    (20, 'Reciprocating saw for demolition, pruning, and rough cuts.', 'Reciprocating Saw', 12, 119.49, 2),
    (21, 'Box of zinc-plated wood screws, assorted sizes for interior use.', 'Wood Screw Assortment', 150, 12.99, 3),
    (22, 'Hex head lag bolts for timber framing and structural fixing.', 'Lag Bolts M10', 220, 18.20, 3),
    (23, 'Nylon wall plugs for masonry and plasterboard installations.', 'Wall Plug Set', 310, 6.75, 3),
    (24, 'Machine screws with nuts and washers in mixed metric sizes.', 'Machine Screw Kit', 140, 19.40, 3),
    (25, 'Pop rivets for sheet metal and lightweight fastening tasks.', 'Aluminum Rivet Pack', 275, 8.60, 3),
    (26, 'Carriage bolts suitable for wood assemblies and benches.', 'Carriage Bolts M8', 190, 11.95, 3),
    (27, 'Anchor bolts for concrete mounting and heavy-duty support.', 'Concrete Anchor Set', 96, 21.70, 3),
    (28, 'Drywall screws with black phosphate coating.', 'Drywall Screw Box', 400, 14.25, 3),
    (29, 'Stainless steel hose clamps for plumbing and automotive tasks.', 'Hose Clamp Pack', 180, 9.15, 3),
    (30, 'Digital multimeter for voltage, resistance, and continuity testing.', 'Digital Multimeter', 44, 27.90, 4),
    (31, 'Non-contact voltage tester pen for safe live wire detection.', 'Voltage Tester Pen', 58, 15.80, 4),
    (32, 'Wire stripper and cutter for electricians and installers.', 'Wire Stripper', 36, 17.45, 4),
    (33, 'Crimping tool for insulated terminals and connectors.', 'Terminal Crimping Tool', 22, 32.00, 4),
    (34, 'Cable ties in assorted lengths for clean cable management.', 'Cable Tie Assortment', 500, 7.20, 4),
    (35, 'Insulation tape multipack in standard electrical colors.', 'Electrical Tape Set', 120, 5.99, 4),
    (36, 'Extension cord reel with overload protection and 25 m cable.', 'Cable Reel 25m', 13, 49.90, 4),
    (37, 'Compact soldering station for electronics repair and prototyping.', 'Soldering Station 60W', 11, 79.00, 4),
    (38, 'Rolling toolbox with metal latches and removable tray.', 'Rolling Toolbox', 15, 64.50, 5),
    (39, 'Wall-mounted pegboard kit for workshop organization.', 'Pegboard Organizer', 20, 44.25, 5),
    (40, 'Plastic parts organizer with transparent drawers for screws and bits.', 'Parts Drawer Cabinet', 18, 36.99, 5),
    (41, 'Stackable storage bins for garage, warehouse, and workshop use.', 'Stackable Storage Bin', 75, 8.40, 5),
    (42, 'Tool backpack with reinforced base and multiple compartments.', 'Tool Backpack', 14, 59.95, 5),
    (43, 'Portable tool case with foam insert for delicate instruments.', 'Protective Tool Case', 9, 82.10, 5),
    (44, 'Metal shelving unit for workshop and stockroom storage.', 'Workshop Shelving Unit', 8, 119.00, 5),
    (45, '5 m tape measure with magnetic hook and belt clip.', 'Tape Measure 5m', 67, 8.95, 6),
    (46, 'Self-leveling laser level for alignment and layout tasks.', 'Laser Level', 13, 112.75, 6),
    (47, 'Stainless steel digital caliper with metric and imperial display.', 'Digital Caliper 150mm', 25, 29.99, 6),
    (48, 'Combination square for marking 90 and 45 degree angles.', 'Combination Square', 33, 14.60, 6),
    (49, 'Spirit level with three vials and aluminum frame.', 'Spirit Level 600mm', 29, 18.30, 6),
    (50, 'Chalk line reel for long straight layout marks.', 'Chalk Line Reel', 22, 11.40, 6),
    (51, 'Infrared thermometer for non-contact temperature readings.', 'Infrared Thermometer', 17, 34.20, 6),
    (52, 'Clear anti-fog safety glasses for workshop and site use.', 'Safety Glasses', 90, 6.50, 7),
    (53, 'Impact-resistant hard hat with adjustable suspension system.', 'Hard Hat', 28, 19.99, 7),
    (54, 'Cut-resistant gloves for handling sheet metal and rough materials.', 'Cut-Resistant Gloves', 54, 12.25, 7),
    (55, 'Disposable dust masks for sanding and light particulate protection.', 'Dust Mask Pack', 160, 9.80, 7),
    (56, 'Over-ear hearing protection earmuffs for loud power tools.', 'Hearing Protection Earmuffs', 24, 21.40, 7),
    (57, 'High-visibility safety vest with reflective strips.', 'Hi-Vis Safety Vest', 37, 8.75, 7),
    (58, 'Bypass pruner for trimming branches and garden plants.', 'Garden Pruner', 26, 15.50, 8),
    (59, 'Round-point shovel for digging and moving soil or gravel.', 'Round Shovel', 19, 27.60, 8),
    (60, 'Leaf rake with lightweight handle for garden cleanup.', 'Leaf Rake', 23, 16.20, 8),
    (61, 'Garden hose with reinforced wall and adjustable nozzle included.', 'Garden Hose 20m', 18, 31.95, 8),
    (62, 'Manual hedge shears for trimming shrubs and hedges.', 'Hedge Shears', 12, 24.80, 8),
    (63, 'Cordless grass trimmer for edges and light overgrowth.', 'Grass Trimmer 20V', 10, 109.99, 8);

-- Keep sequences aligned in case inserts rely on nextval later
SELECT setval('category_seq', 9, false);
SELECT setval('product_seq', 64, false);

COMMIT;