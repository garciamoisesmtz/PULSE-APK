package com.pulse.app

object Ex {
    val marcha = Exercise(
        "marcha", "Marcha en el lugar", Anims.marcha, Kind.TIMED, seconds = 90,
        steps = listOf(
            "Párate derecho con los pies al ancho de la cadera.",
            "Sube una rodilla y luego la otra a un ritmo cómodo.",
            "Mueve los brazos al contrario de las piernas, como al caminar.",
            "Respira normal y ve subiendo el ritmo poco a poco.",
        ),
        cue = "Es para entrar en calor, no para cansarte.",
    )
    val circulos = Exercise(
        "circulos", "Círculos de brazos", Anims.circulos, Kind.TIMED, seconds = 40,
        steps = listOf(
            "Abre los brazos a la altura de los hombros.",
            "Haz círculos pequeños hacia adelante durante 20 segundos.",
            "Cambia la dirección hacia atrás los otros 20 segundos.",
        ),
        cue = "Hombros abajo, lejos de las orejas.",
    )
    val sentadillaLibre = Exercise(
        "sentadilla_libre", "Sentadilla sin peso", Anims.sentadillaLibre, Kind.TIMED, seconds = 40,
        steps = listOf(
            "Pies al ancho de los hombros, puntas un poco hacia afuera.",
            "Lleva la cadera hacia atrás y abajo, como si te sentaras.",
            "Baja hasta donde estés cómodo, con el pecho arriba.",
            "Sube empujando el piso con todo el pie. Hazlas lentas.",
        ),
        cue = "Las rodillas siguen la dirección de las puntas.",
    )
    val cadera = Exercise(
        "cadera", "Rotación de cadera", Anims.cadera, Kind.TIMED, seconds = 40,
        steps = listOf(
            "Manos en la cintura y rodillas un poco flexionadas.",
            "Dibuja círculos amplios con la cadera hacia un lado.",
            "A los 20 segundos cambia de dirección.",
        ),
        cue = "El torso se queda quieto; solo se mueve la cadera.",
    )
    val goblet = Exercise(
        "goblet", "Sentadilla con peso al pecho", Anims.goblet, Kind.REPS, reps = "12–15", rest = 75,
        steps = listOf(
            "Sostén un disco, mancuerna o garrafón de agua pegado al pecho.",
            "Pies al ancho de los hombros, puntas un poco hacia afuera.",
            "Baja llevando la cadera atrás, con la espalda recta.",
            "Sube empujando con los talones y exhala al subir.",
        ),
        cue = "Si no tienes peso, hazla sin peso y más lenta.",
    )
    val pressPecho = Exercise(
        "press_pecho", "Press de pecho", Anims.pressPecho, Kind.REPS, reps = "10–12", rest = 75, tracksLoad = true,
        steps = listOf(
            "Ajusta el asiento para que los mangos queden a la altura del pecho.",
            "Espalda pegada al respaldo y pies firmes en el piso.",
            "Empuja hacia adelante exhalando, sin trabar los codos.",
            "Regresa lento, en unos 2 segundos, sin que las placas choquen.",
        ),
        cue = "Haz una serie ligera de 10 antes de las series reales.",
    )
    val jalon = Exercise(
        "jalon", "Jalón al pecho", Anims.jalon, Kind.REPS, reps = "10–12", rest = 75, tracksLoad = true,
        steps = listOf(
            "Toma la barra alta un poco más abierta que tus hombros.",
            "Siéntate con los muslos bajo los rodillos.",
            "Jala la barra hacia la parte alta del pecho llevando los codos hacia abajo.",
            "Sube la barra lento hasta estirar los brazos.",
        ),
        cue = "Pecho arriba; no jales con impulso ni te balancees.",
    )
    val extension = Exercise(
        "extension", "Extensión de pierna", Anims.extension, Kind.REPS, reps = "12", rest = 60, tracksLoad = true,
        steps = listOf(
            "Siéntate y coloca los tobillos detrás del rodillo inferior.",
            "Agárrate del asiento a los lados.",
            "Estira las piernas hasta casi extenderlas y aprieta el muslo un segundo.",
            "Baja lento, sin dejar caer el peso.",
        ),
        cue = "Empieza con poco peso: la rodilla agradece el control.",
    )
    val curlFemoral = Exercise(
        "curl_femoral", "Curl de pierna de pie", Anims.curlFemoral, Kind.REPS, reps = "12 por pierna", rest = 60, tracksLoad = true,
        steps = listOf(
            "Pon la tobillera en la polea baja y ajústala a un tobillo.",
            "Párate frente a la máquina y sujétate del marco.",
            "Dobla la rodilla llevando el talón hacia el glúteo.",
            "Baja lento. Termina las repeticiones y cambia de pierna.",
        ),
        cue = "Las rodillas quedan juntas; solo se mueve la pantorrilla.",
    )
    val plancha = Exercise(
        "plancha", "Plancha", Anims.plancha, Kind.TIMED, seconds = 30, rest = 45,
        steps = listOf(
            "Apoya antebrazos y puntas de los pies en el piso.",
            "Codos debajo de los hombros.",
            "Cuerpo en línea recta de la cabeza a los talones.",
            "Aprieta abdomen y glúteos y respira normal.",
        ),
        cue = "Si la espalda baja se hunde, apoya las rodillas.",
    )
    val marchaAlta = Exercise(
        "marcha_alta", "Marcha alta", Anims.marchaAlta, Kind.TIMED, seconds = 360,
        steps = listOf(
            "Marcha en el lugar subiendo las rodillas a la altura de la cadera.",
            "Mantén un ritmo en el que puedas hablar pero no cantar.",
            "Si te cansas mucho, baja la altura de las rodillas sin parar.",
        ),
        cue = "Este cardio final aprovecha el gasto de las pesas.",
    )
    val zancadas = Exercise(
        "zancadas", "Zancadas", Anims.zancadas, Kind.REPS, reps = "10 por pierna", rest = 75,
        steps = listOf(
            "Párate derecho y da un paso largo hacia adelante.",
            "Baja hasta que la rodilla de atrás casi toque el piso.",
            "Empuja con el pie de adelante para regresar.",
            "Alterna piernas. Sujétate del marco de la máquina si pierdes el equilibrio.",
        ),
        cue = "La rodilla de adelante no pasa mucho más allá de la punta del pie.",
    )
    val remo = Exercise(
        "remo", "Remo sentado", Anims.remo, Kind.REPS, reps = "10–12", rest = 75, tracksLoad = true,
        steps = listOf(
            "Pon la barra corta en la polea baja.",
            "Siéntate en el piso con los pies apoyados en la base de la máquina.",
            "Jala la barra hacia el ombligo llevando los codos atrás.",
            "Aprieta la espalda un segundo y regresa lento.",
        ),
        cue = "La espalda se queda recta; no te vayas hacia atrás.",
    )
    val aperturas = Exercise(
        "aperturas", "Aperturas de pecho", Anims.aperturas, Kind.REPS, reps = "10–12", rest = 75, tracksLoad = true,
        steps = listOf(
            "Siéntate con la espalda pegada al respaldo.",
            "Apoya los antebrazos en las almohadillas verticales.",
            "Junta los brazos al frente apretando el pecho.",
            "Abre lento hasta sentir un estiramiento suave.",
        ),
        cue = "No abras más allá de la línea de los hombros.",
    )
    val laterales = Exercise(
        "laterales", "Elevaciones laterales", Anims.laterales, Kind.REPS, reps = "12", rest = 60, tracksLoad = true,
        steps = listOf(
            "Toma el mango de la polea baja con la mano contraria a la máquina.",
            "Párate de lado a la máquina.",
            "Sube el brazo hacia el lado hasta la altura del hombro.",
            "Baja lento. Termina y cambia de brazo.",
        ),
        cue = "Usa poco peso: es un músculo pequeño.",
    )
    val curlBiceps = Exercise(
        "curl_biceps", "Curl de bíceps", Anims.curlBiceps, Kind.REPS, reps = "12", rest = 45, tracksLoad = true,
        steps = listOf(
            "Toma la barra corta de la polea baja con las palmas hacia arriba.",
            "Codos pegados al cuerpo.",
            "Sube la barra doblando los codos, sin mover los hombros.",
            "Baja lento hasta estirar los brazos.",
        ),
        cue = "Si te balanceas para subir, baja el peso.",
    )
    val triceps = Exercise(
        "triceps", "Extensión de tríceps", Anims.triceps, Kind.REPS, reps = "12", rest = 45, tracksLoad = true,
        steps = listOf(
            "Toma la barra de la polea alta con las palmas hacia abajo.",
            "Codos pegados a los costados.",
            "Empuja la barra hacia abajo hasta estirar los brazos.",
            "Sube lento hasta la altura del pecho.",
        ),
        cue = "Solo se mueven los antebrazos.",
    )
    val puente = Exercise(
        "puente", "Puente de glúteo", Anims.puente, Kind.REPS, reps = "15", rest = 45,
        steps = listOf(
            "Acuéstate boca arriba con las rodillas dobladas y los pies apoyados.",
            "Sube la cadera apretando los glúteos.",
            "Mantén un segundo arriba.",
            "Baja lento sin apoyar del todo la cadera.",
        ),
        cue = "Empuja con los talones, no con las puntas.",
    )
    val caminata = Exercise(
        "caminata", "Caminata rápida", Anims.caminata, Kind.TIMED, seconds = 25 * 60,
        steps = listOf(
            "Camina afuera a paso rápido, o marcha en casa si no puedes salir.",
            "Ritmo en el que puedas hablar pero no cantar.",
            "Puedes ir con tu familia; lo que cuenta es mantener el ritmo.",
        ),
        cue = "Este es el cardio que más ayuda a bajar triglicéridos.",
    )
    val caminataLarga = Exercise(
        "caminata_larga", "Caminata o juego activo", Anims.caminata, Kind.TIMED, seconds = 30 * 60,
        steps = listOf(
            "Camina, anda en bici o juega activamente con tus hijos.",
            "La meta es moverte de forma continua.",
            "No hace falta que sea intenso.",
        ),
        cue = "El día activo cuenta para tu racha.",
    )
    val planchaLateral = Exercise(
        "plancha_lateral", "Plancha lateral", Anims.planchaLateral, Kind.TIMED, seconds = 20, rest = 30,
        steps = listOf(
            "Acuéstate de lado apoyado en el antebrazo.",
            "Sube la cadera hasta formar una línea recta.",
            "Aguanta y luego repite del otro lado en la misma serie.",
        ),
        cue = "Si es mucho, apoya la rodilla de abajo.",
    )
    val bichoMuerto = Exercise(
        "bicho_muerto", "Bicho muerto", Anims.bichoMuerto, Kind.REPS, reps = "10 por lado", rest = 30,
        steps = listOf(
            "Boca arriba, brazos hacia el techo y rodillas dobladas a 90°.",
            "Estira un brazo hacia atrás y la pierna contraria al frente.",
            "Regresa y cambia de lado.",
            "Mantén la espalda baja pegada al piso todo el tiempo.",
        ),
        cue = "Lento y controlado vale más que rápido.",
    )
}
