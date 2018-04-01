<?php

// API:
//   Method:  POST
//   Body:    Contains LaTeX code to be compiled.
//   NOTE:    The query parameter 'token' contains a secret token to validate
//            whether the client is allowed to compile LaTeX code.
//   Results: 200: OK
//              Body: Contains image in PNG format
//            400: Code is invalid (LaTeX Error)
//            401: Wrong token.
//            405: Wrong HTTP method.
//            501: Internal Server Error

if (!isset($_SERVER['REQUEST_METHOD']) || $_SERVER['REQUEST_METHOD'] !== 'POST') {
    // Do nothing.
    http_response_code(405);
    exit;
}

if (!isset($_GET['token']) || $_GET['token'] !== 'SECRET_TOKEN') {
    http_response_code(401);
    exit;
}

// Create temporary directory.
$tempdir = tempnam(sys_get_temp_dir(), 'kitex-');
// tempnam _creates_ a temporary file --> delete it
if (file_exists($tempdir)) {
    unlink($tempdir);
}
mkdir($tempdir);

$code = file_get_contents("php://input");

$tex_file_basename = 'file';
$tex_file_name = "$tex_file_basename.tex";
$tex_file = "$tempdir/$tex_file_name";
$pdf_file = "$tempdir/$tex_file_basename.pdf";

file_put_contents($tex_file, "% Generated file by KiTeX.
\\documentclass{standalone}

\\usepackage[T1]{fontenc}
\\usepackage[utf8]{inputenc}

\\begin{document}
    $code
\\end{document}
");

// TODO: Error handling.

$latex_result = null;
$latex_return_code = 0;
exec("cd '$tempdir' && pdflatex -interaction errorstopmode -halt-on-error -file-line-error $tex_file_name", $latex_result, $latex_return_code);
if ($latex_return_code !== 0) {
    http_response_code(400);
    exit;
}

header('Content-Type: application/pdf');
echo file_get_contents($pdf_file);

// Delete temporary directory.
unlink($tempdir);

?>
