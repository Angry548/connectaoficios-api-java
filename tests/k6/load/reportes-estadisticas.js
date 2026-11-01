import { configuration, validate, run } from '../common/scenarios.js';
export const options=configuration(false);
export function setup(){validate('reportes-estadisticas');}
export default function(){run('reportes-estadisticas');}
