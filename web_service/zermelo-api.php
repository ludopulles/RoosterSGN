<?php

require('ZermeloRoosterPHP/Cache.php');
require('ZermeloRoosterPHP/Zermelo.php');

class TempCache extends ZermeloApi\Cache
{
	private $cache = array();

	public function __construct() {}

	public function saveToken($user, $token) {
		$this->cache[$user] = $token;
	}

	public function getToken($id) {
		return array_key_exists($id, $this->cache) ? $this->cache[$id] : NULL;
	}

	public function clearCache($a = false) {
		$this->cache = array();
	}

	public function setFileLocation($location) {}
	public function getFileLocation() { return 'cache.json'; }
}

function dtf($format, $date)
{
	return strftime($format, $date->getTimestamp());
}

/* formaat return type:
data = {
	wijzigingen: [ string, ... ],
	rooster: {
		d1: {
			u1: {
			}, u2: {}, ...
		},
		...
		d5: []
	}
}
 */

function format_roostersgn($response, $requestedWeek)
{
	$data = array(
		'week' => $requestedWeek,
		'timestamp' => time(),
		'wijzigingen' => array(),
		'rooster' => NULL
//		'aantal_uren' => 0,
//		'vakken' => '',
//		'vakantie' => TRUE
	);

	// $data['rooster'] == NULL => invalid user/password

	$weeks = array();
	$data['rooster'] = array();
	for ($d = 1; $d <= 5; $d++) {
		for ($u = 1; $u <= 8; $u++) {
			$data['rooster']['d' . $d]['u' . $u] = array();
		}
	}

	foreach ($response['data'] as $lesuur)
	{
		if (!$lesuur['valid'])
			continue;

		// $obj = array('key' => $key, 'value' => $lesuur);
		// $data['rooster'][$key] = $obj;

		$uur = $lesuur['startTimeSlotName'];
		$datetime = new DateTime('now');
		$datetime->setTimestamp((int) ($lesuur['start'] + $lesuur['end']) / 2);
		$dag = 'd' . dtf('%u', $datetime);
		$weeks[dtf('%V', $datetime)] = true;

		$wijziging = $lesuur['changeDescription'];
		if ($wijziging !== "") {
			$data['wijzigingen'][] = dtf('%a', $datetime) . " " . $uur . ": " . $wijziging;
		}

		$les = array(
			'docent' => implode(' ', $lesuur['teachers']),
			'vak' => implode(' ', $lesuur['subjects']),
			'lokaal' => implode(' ', $lesuur['locations']),
			'klas' => implode(' ', $lesuur['groups']),
			'vervallen' => $lesuur['cancelled']
		);
		$data['rooster'][$dag][$uur][] = $les;
	}

	$weeks = array_keys($weeks);
	if (count($weeks) == 1) {
		// Unique week defined:
		$data['week'] = $weeks[0];
	}

	for ($d = 1; $d <= 5; $d++) {
		for ($u = 1; $u <= 8; $u++) {
			if (empty($data['rooster']['d' . $d]['u' . $u])) {
				$data['rooster']['d' . $d]['u' . $u] = 'vrij';
			} else if (count($data['rooster']['d' . $d]['u' . $u]) == 1 &&
					$data['rooster']['d' . $d]['u' . $u][0]['vervallen']) {
				$data['rooster']['d' . $d]['u' . $u] = 'vervallen';
			} else {
				foreach ($data['rooster']['d' . $d]['u' . $u] as &$les)
					unset($les['vervallen']);
			}
		}
	}

	return $data;
}

function processRequest($user, $pass, $school, $week)
{
	if (empty($user)) {
		return array(
			'code' => -1,
			'melding' => "onbekende gebruiker",
		);
	}

	$zermelo = new ZermeloAPI($school, TRUE, new TempCache());

	$success = $zermelo->grabAccessToken($user, $pass, true);

	if ($success === FALSE) {
		// incorrect password

		// for legacy reasons:
		return array(
			'week' => NULL,
			'rooster' => NULL,
			'timestamp' => time(),
			'user' => $user
		);
	}

	$start = strtotime('last monday', strtotime('tomorrow'));
	$end = strtotime('next saturday', strtotime('tomorrow'));
	$cur_week = date('W', $start);

	$start += 2 * 3600;
	$end += 2 * 3600;

	$oneweek = 7 * 24 * 3600;
	if ($week >= 30 && $cur_week < 30) {
		// go back to last year
		while (date('W', $start) != $week) {
			$start -= $oneweek;
			$end -= $oneweek;
		}
	} else if ($week < 30 && $cur_week >= 30) {
		// go forward to next year
		while (date('W', $start) != $week) {
			$start += $oneweek;
			$end += $oneweek;
		}
	} else {
		while (date('W', $start) < $week) {
			$start += $oneweek;
			$end += $oneweek;
		}
		while (date('W', $start) > $week) {
			$start -= $oneweek;
			$end -= $oneweek;
		}
	}

	$response = $zermelo->getAppointments($user, $start, $end);
	if ($response['status'] != 200) {
		return array(
			'code' => $response['status'],
			'melding' => $response['message']
		);
	}

	// header('Content-Type: application/json');
	// echo json_encode($response); exit(0);

	$ourResponse = format_roostersgn($response, $week);
	$zermelo->invalidateAccessToken($user);
	return $ourResponse;
}


// Set-up necessary things
date_default_timezone_set("Europe/Amsterdam");
setlocale(LC_ALL, 'nl_NL');

function tryPar($key)
{
	if (array_key_exists($key, $_POST))
		return $_POST[$key];
	if (array_key_exists($key, $_GET))
		return $_GET[$key];
	return NULL;
}

$user = tryPar('user');
$pass = tryPar('pass');
$school = tryPar('school');
$week = tryPar('week');

if (empty($school)) {
	$school = 'stedelijkgymnijmegen';
}

if (!empty($week)) {
	$week = (int) $week;
	if ($week <= 0 || $week >= 53)
		unset($week);
}

if (empty($week)) {
	$week = date("W");
	if (date("N") >= 6) $week++;
}

$result = processRequest($user, $pass, $school, $week);

header('Content-Type: application/json');
echo json_encode($result);
die();

