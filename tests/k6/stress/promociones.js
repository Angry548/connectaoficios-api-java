import { configuration, validate, run } from '../common/scenarios.js';
export const options=configuration(true);
export function setup(){validate('promociones');}
export default function(){run('promociones');}
