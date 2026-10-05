The AUV (Autonomous Underwater Vehicle) is a portable untethered remotely operated vehicle, equipped with a visual mapping system and verified on-board autonomy. The AUV is capable of conducting light intervention tasks, such as cathodic protection surveys (oil and gas) and simple coring (offshore).

The LRE (Last Response Engine) is the safety controller for the AUV. It operates as a reactive controller with four operating modes:
- OCM (Operator Control Mode): Direct operator passthrough control.
- MOM (Main Operating Mode): Autonomous navigation at 1 m/s with obstacle monitoring.
- HCM (High Caution Mode): Reduced speed (0.1 m/s) when near static obstacles.
- CAM (Collision Avoidance Mode): Emergency collision avoidance when a dynamic obstacle is on a collision course.

The LRE reads sensor data (AUV depth, velocity, obstacle positions) and sends commands to actuators (advised velocity, advised heading). It continuously monitors for obstacle proximity and collision risk, transitioning between modes based on safety conditions.
